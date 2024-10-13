package com.springcloud.demo.bookingsmicroservice.monitoring;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class XRayFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Segment segment = AWSXRay.getCurrentSegment();
        String traceId = segment.getTraceId().toString();
        String parentId = segment.getId();

        String xrayHeader = String.format("Root=%s;Parent=%s;Sampled=1", traceId, parentId);
        requestTemplate.header("X-Amzn-Trace-Id", xrayHeader);
    }
}
