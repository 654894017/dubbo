package com.test.swift;

import org.apache.thrift.TException;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;


@ThriftService("Example")
public interface Example
{
    @ThriftMethod(value = "sayHello")
    String sayHello(
        @ThriftField(value=1, name="yourname") final String yourname
    ) throws org.apache.thrift.TException;
}