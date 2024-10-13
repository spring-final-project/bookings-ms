package com.springcloud.demo.bookingsmicroservice.monitoring;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TracingExceptions {

    public void addExceptionMetadata(String message){
        Segment segment = AWSXRay.getCurrentSegment();
        if(segment != null) {
            segment.setMetadata(Map.of("exception", Map.of("message", message)));
        }
    }
}
