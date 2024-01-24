package cn.cetasas.db.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = new RestTemplate(factory);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_OCTET_STREAM)); // application/octet-stream
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }

//    @Bean("restTemplate")
//    public RestTemplate restTemplate(){
//        RestTemplate restTemplate = new RestTemplate();
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(
//                MediaType.TEXT_HTML,
//                MediaType.TEXT_PLAIN));
//        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
//
//        return restTemplate;
//    }


//    @Bean
//    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setConnectTimeout(15000);
//        factory.setReadTimeout(5000);
//        return factory;
//    }

    @Bean
    public HttpComponentsClientHttpRequestFactory generateHttpRequestFactory()
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException
    {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return factory;
    }
}
