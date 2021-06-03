////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: NotificationConfig.java
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Author: Arindam
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

package com.nseindia.mc.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for Notification Service. */
@Configuration
@ConfigurationProperties("notification")
@Data
@EnableConfigurationProperties
@NoArgsConstructor
public class NotificationConfig {
  private String funcOrServiceID;
  private String funcOrServiceName;
  private String invocationType;
  private String bulkId;
  private String from;
  private String text;
  private String html;
  private String replyTo;
  private int templateId;
  private String intermediateReport;
  private String emailSubject;
  private String emailContent;
  private String apApprovalHtml;
  private String apApprovalSubject;
}
