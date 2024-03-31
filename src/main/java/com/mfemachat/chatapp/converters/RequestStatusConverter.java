package com.mfemachat.chatapp.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import com.mfemachat.chatapp.models.RequestStatus;

public class RequestStatusConverter implements Converter<RequestStatus, String> {

    @Override
    @Nullable
    public String convert(@SuppressWarnings("null") RequestStatus source) {
        return source.name();
    }

}
