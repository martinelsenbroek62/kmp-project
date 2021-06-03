package com.nseindia.mc.config;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: SwaggerConfig.java
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Author: Shailesh
//
// NSE - Confidential
// Do not use, distribute, or copy without consent of NSE.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// Copyright (c) 2020 NSE. All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Member Compliance",
        "Endpoints for change in aiml service.",
        "1.0",
        "Terms of service",
        new Contact("NSE", "https://www.nseindia.com", "info@nseinfobase.com"),
        "License of API",
        "API license URL",
        Collections.emptyList());
  }
}
