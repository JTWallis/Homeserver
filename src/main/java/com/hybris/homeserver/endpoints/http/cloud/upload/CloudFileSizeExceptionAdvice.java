package com.hybris.homeserver.endpoints.http.cloud.upload;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@ControllerAdvice
public class CloudFileSizeExceptionAdvice {
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleFileSizeExceeded(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("uploadErrorMsg", "File too big (max. 4MB) !");
		return "redirect:/cloud";
	}
}
