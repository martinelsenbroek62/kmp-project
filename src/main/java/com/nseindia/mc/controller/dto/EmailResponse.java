////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: EmailResponse.java
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

package com.nseindia.mc.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailResponse {
  private Long id;
  private String bulkId;
  private String callServiceName;
  private String comments;
  private String delReqMessage;
  private String delResMessage;
  private String delSentRecieveDate;
  private String errorCode;
  private String errorMessage;
  private String from;
  private String to;
  private String messageId;
  private String modifiedDate;
  private String notServiceType;
  private String reqMessage;
  private String resMessage;
  private String sentDate;
  private String serviceId;
  private String spanId;
  private String status;
  private String traceId;
  private String transId;
}
