package com.file_upload_service.config;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.comm.Protocol;
import com.file_upload_service.entity.po.Client;
import com.file_upload_service.setting.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@EnableConfigurationProperties(OSSProperties.class)
public class OSSAutoConfiguration {

    @Autowired
    private OSSProperties ossProperties;

    private OSSClient ossClientForUpload;

    private OSSClient ossClientForManager;

    @PreDestroy
    public void close() {
        if (this.ossClientForUpload != null) {
            this.ossClientForUpload.shutdown();
        }
        if (this.ossClientForManager != null) {
            this.ossClientForManager.shutdown();
        }
    }

    @Bean
    public ClientConfiguration clientConfiguration() {
        Client client = new Client();
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxConnections(client.getMaxConnections());
        configuration.setSocketTimeout(client.getSocketTimeout());
        configuration.setConnectionTimeout(client.getConnectionTimeout());
        configuration.setConnectionRequestTimeout(client.getConnectionRequestTimeout());
        client.setIdleConnectionTime(client.getIdleConnectionTime());
        configuration.setMaxErrorRetry(client.getMaxErrorRetry());
        configuration.setSupportCname(client.isSupportCname());
        configuration.setSLDEnabled(client.isSldEnabled());
        if (Protocol.HTTP.toString().equals(client.getProtocol())) {
            configuration.setProtocol(Protocol.HTTP);
        } else if (Protocol.HTTPS.toString().equals(client.getProtocol())) {
            configuration.setProtocol(Protocol.HTTPS);
        }
        configuration.setUserAgent(client.getUserAgent());

        return configuration;
    }

    @Bean(name = "ossClientForUpload")
    public OSSClient ossClientForUpload(ClientConfiguration clientConfiguration) {
        ossClientForUpload = new OSSClient(ossProperties.getEndpointUpload(), ossProperties.getAccessKeyId(), ossProperties
                .getAccessKeySecret(), clientConfiguration);
        return ossClientForUpload;
    }

    @Bean(name = "ossClientForManager")
    public OSSClient ossClientForManager(ClientConfiguration clientConfiguration) {
        ossClientForManager = new OSSClient(ossProperties.getEndpointManager(), ossProperties
                .getAccessKeyId(), ossProperties.getAccessKeySecret(), clientConfiguration);
        return ossClientForManager;
    }
}
