////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: EmailResponse.java
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Author: NotificationServiceProxy
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

package com.nseindia.mc.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.nseindia.mc.controller.dto.EmailRequestVO;
import com.nseindia.mc.controller.dto.EmailResponse;

/** Client to call notification service. */
@FeignClient(name = "notification-service")
public interface NotificationServiceProxy {

  @PostMapping("/api/v1/notification/email/")
  List<EmailResponse> sendEmail(@RequestBody EmailRequestVO emailRequest);

  @PostMapping(
      value = "/api/v1/notification/email/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  List<EmailResponse> sendOTPViaEmailWithAttachment(
      @RequestPart("emailRequest") EmailRequestVO emailRequest,
      @RequestPart(value = "file") MultipartFile file);
}
