package com.s3.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import com.s3.component.S3PresignedUrlGenerator;

@AutoConfiguration
@Import({S3Config.class, S3PresignedUrlGenerator.class})
public class S3AutoConfig {
}
