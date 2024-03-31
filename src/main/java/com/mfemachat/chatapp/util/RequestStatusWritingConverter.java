package com.mfemachat.chatapp.util;

import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.EnumWriteSupport;

import com.mfemachat.chatapp.models.RequestStatus;

@WritingConverter
public class RequestStatusWritingConverter extends EnumWriteSupport<RequestStatus> {}
