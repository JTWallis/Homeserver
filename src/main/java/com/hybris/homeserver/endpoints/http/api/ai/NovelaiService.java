package com.hybris.homeserver.endpoints.http.api.ai;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.djl.sentencepiece.SpTokenizer;

@Service
public class NovelaiService {
	// Most models like Kayra, Euterpe are on endpoint text.novelai.net/ai/generate
	// GLM-4.6 is only available via OpenAI-compatible API text.novelai.net/oa/v1/
	//  -> Calling /ai/generate with models newer than GLM-4.5 will give 403 error.
	
	private final String SUBDIR_NOVELAI 		= "novelai/";
	private final String URL 					= "https://text.novelai.net";
	private final String URL_STANDARD_TEXTGEN	= URL + "/ai/generate";
	private final String URL_OAI_TEXTGEN 		= URL + "/oa/v1/completions";
	
	@Value("${novelai.key}")
	private String apiKey;
	
	private static final Logger logger = LoggerFactory.getLogger(NovelaiService.class);
	
	public NovelaiResponse prompt(String requestJson, String requestUrl) {
		try {
			logger.debug("Sending request body: " + requestJson);
			 
			NovelaiResponse response = sendRequest(requestJson, requestUrl);
			
			HttpStatus status = HttpStatus.valueOf(response.getStatusCode());
			if(status.is4xxClientError()) {
				return new NovelaiResponse(
						response.getStatusCode(),
						parseResponseError(response.getMessage())
				);
			}
			
			return response;
		} catch (URISyntaxException e) {
			logger.error("Could not send request to URL " + requestUrl + ": " + e.getMessage());
			return null;
		} catch (IOException e) {
			logger.error("Got IOException in NovelAI prompt for URL " + requestUrl + ": " + e.getMessage());
			return null;
		}
	}
	
	public NovelaiResponse promptGlm(NovelaiPromptDto promptDto) {
		// Ensure newline after user input, to hopefully restricten the AI going rogue.
		String userInput = promptDto.getPrompt();
		if(!userInput.endsWith("\n")) {
			userInput = userInput + "\n";
		}
		
		String requestJson;
		try {
			requestJson = loadConfigGlm(userInput);
		} catch(IOException e) {
			logger.error("Could not load GLM config file: " + e.getMessage());
			return null;
		}
		
		NovelaiResponse response = prompt(requestJson, URL_OAI_TEXTGEN);
		
		if(response == null) {
			return null;
		}
		
		if(HttpStatus.valueOf(response.getStatusCode()).isError()) {
			return response;
		}
		
		String output = parseResponseOaiCompletion(response.getMessage()).trim();
		logger.debug("Raw GLM output:\n\"\"\"\n" + output + "\n\"\"\"");
		
		// System prompt gave explicit rule to receive questions, starting with "User:"
		//   and give responses, starting with "Assistant:".
		//   Sometimes special phrases like "</think>" might bleed into the response,
		//   or the AI is cheeky enough to ask and answer its own questions afterwards.
		// Cut off all of these unwanted parts of the response.
		// Further testing in tweaking the config and System prompt is needed,
		//   to prevent these special phrases and the AI basically interviewing itself.
		String prefixAssistant = "Assistant:";
		int prefixIndex = output.indexOf(prefixAssistant);
		if(prefixIndex >= 0) {
			output = output.substring(prefixIndex + prefixAssistant.length());
		}
		
		String suffixUser = "User:";
		int suffixIndex = output.indexOf(suffixUser);
		if(suffixIndex >= 0) {
			output = output.substring(0, suffixIndex);
		}
		
		output = output.trim();
		return new NovelaiResponse(response.getStatusCode(), output);
	}
	
	public NovelaiResponse promptKayra(NovelaiPromptDto promptDto) {
		// Inputs and outputs to the endpoint /ai/generate have to encode to- and decode from Base64.
		// Additionally, before the encoding and after decoding,
		//   the input and output have to be tokenized and detokenized.
		//   The exact Tokenizer-model varies depending on the Textgen-model.
		//   For now, only the use of Kayra-v1 is supported, which uses the Tokenizer-model Nerdstash-V2.
		try (SpTokenizer sp = new SpTokenizer(getFile("nerdstashv2.model").toPath()) ) {
			String encodedInput = encodeTokenize(promptDto.getPrompt(), sp);
			
			String requestJson = loadConfigKayra(encodedInput);
			NovelaiResponse response = prompt(requestJson, URL_STANDARD_TEXTGEN);
			
			if(response == null) {
				return null;
			}
			
			if(HttpStatus.valueOf(response.getStatusCode()).isError()) {
				return response;
			}
			
			String output = parseResponseStandard(response.getMessage());
			String decoded = decodeDetokenize(output, sp);
			
			return new NovelaiResponse(response.getStatusCode(), decoded);	
		} catch (IOException e) {
			logger.error("Could not read NerdstashV2 model file: " + e.getMessage());
			return null;
		}
	}
	
	private NovelaiResponse sendRequest(String requestJson, String url) throws URISyntaxException, IOException {
		// Open HTTPS connection with configured Request method and Headers.
		HttpsURLConnection conn = createConnection(url);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "Bearer " + apiKey);
		
		// Send POST request.
		try ( DataOutputStream dos = new DataOutputStream(conn.getOutputStream()) ) {
			dos.write(requestJson.getBytes(StandardCharsets.UTF_8));
			dos.flush();
		}

		int responseCode = conn.getResponseCode();
		
		// Get correct InputStream based on if response is successful or error.
		boolean successfulResponse =
				( (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK)
				&& conn.getInputStream() != null );
		
		boolean errorResponse = (conn.getErrorStream() != null);
		
		InputStream inputStream;
		if(successfulResponse) {
			inputStream = conn.getInputStream();
		} else if(errorResponse) {
			inputStream = conn.getErrorStream();
		} else {
			logger.error("Response has neither successful InputStream nor ErrorStream");
			return null;
		}
		
		// Read response content and close connection.
		String responseJson;
		try ( BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)) ) {
			responseJson = br.lines().collect(Collectors.joining("\n"));
		}
		
		conn.disconnect();

		logger.debug("ResponseJson: " + responseJson);
		return new NovelaiResponse(responseCode, responseJson);
	}
	
	private String parseResponseStandard(String responseJson) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node;
		try {
			node = (ObjectNode) mapper.readTree(responseJson);
		} catch (JsonProcessingException e) {
			return null;
		}
		
		return node.get("output").asText();
	}
	
	private String parseResponseOaiCompletion(String responseJson) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node;
		try {
			node = (ObjectNode) mapper.readTree(responseJson);
		} catch (JsonProcessingException e) {
			return null;
		}
		
		return node.get("choices").get(0)
				.get("text").asText();
	}
	
	private String parseResponseError(String responseJson) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node;
		try {
			node = (ObjectNode) mapper.readTree(responseJson);
		} catch (JsonProcessingException e) {
			return null;
		}
		
		return node.get("error")
				.get("message").asText();
	}
	
	private HttpsURLConnection createConnection(String endpoint) throws URISyntaxException, IOException {
		URL url = new URI(endpoint).toURL();
		
		return (HttpsURLConnection) url.openConnection();
	}
	
	private String decodeDetokenize(String rawOutput, SpTokenizer sp) {
		// Decode from Base64
		byte[] decodedBytes = Base64.getDecoder().decode(rawOutput);
		

		ByteBuffer buffer = ByteBuffer.wrap(decodedBytes).order(ByteOrder.LITTLE_ENDIAN);
		
		// Unpack from little-endian uint16.
		int[] tokens = new int[decodedBytes.length / 2];
		for(int i = 0; i < tokens.length; i++) {
			tokens[i] = buffer.getShort() & 0xFFFF;
		}
		
		// Detokenize	
		return sp.getProcessor().decode(tokens);
	}
	
	private String encodeTokenize(String input, SpTokenizer sp) {
		// Tokenize
		int[] tokens;
		tokens = sp.getProcessor().encode(input);
		
		// Pack into little-endian uint16.
		ByteBuffer buffer = ByteBuffer.allocate(tokens.length * 2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		for(int token : tokens) {
			buffer.putShort((short) token);
		}
		
		byte[] binary = buffer.array();
		
		// Encrypt to Base64
		return Base64.getEncoder().encodeToString(binary);
	}
	
	private String loadConfigJson(String userInput, String configFilename, String configInputParamName) throws IOException {		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = (ObjectNode) mapper.readTree(getFile(configFilename));

		String inputFromJson = node.get(configInputParamName).asText();
		inputFromJson += userInput;
		node.put(configInputParamName, inputFromJson);
		
		return mapper.writeValueAsString(node);
	}
	
	private String loadConfigKayra(String userInput) throws IOException {
		return loadConfigJson(userInput, "novelaiKayraConfig.json", "input");
	}
	
	private String loadConfigGlm(String userInput) throws IOException {
		return loadConfigJson(userInput, "novelaiGlmConfig.json", "prompt");
	}

	private File getFile(String filename) throws IOException {
		File file = ResourceUtils.getFile("classpath:" + SUBDIR_NOVELAI + filename);
		if(!file.exists()) return null;
		
		return file;
	}
	
}
