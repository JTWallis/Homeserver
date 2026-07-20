package com.hybris.homeserver.database;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable, Serializable {
	private static final long serialVersionUID = 1L;
	private final int limit;
	private final long offset;
	private final Sort sort;
	
	public OffsetBasedPageRequest(long offset, int limit, Sort sort) {
		if(offset < 0) offset = 0;
		if(limit < 1) limit = 1;
		
		this.offset = offset;
		this.limit = limit;
		this.sort = sort;
	}
	
	@Override
	public int getPageNumber() {
		return (int) (offset / limit);
	}

	@Override
	public int getPageSize() {
		return limit;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public Sort getSort() {
		return sort;
	}

	@Override
	public Pageable next() {
		return new OffsetBasedPageRequest(offset + limit, limit, sort);
	}

	@Override
	public Pageable previousOrFirst() {
		return hasPrevious() ? new OffsetBasedPageRequest(offset - limit, limit, sort) : first();
	}

	@Override
	public Pageable first() {
		return new OffsetBasedPageRequest(0, limit, sort);
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return new OffsetBasedPageRequest((long) pageNumber * limit, limit, sort);
	}

	@Override
	public boolean hasPrevious() {
		return offset > 0;
	}
}
