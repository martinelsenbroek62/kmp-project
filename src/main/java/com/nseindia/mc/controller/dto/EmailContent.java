////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: EmailContent.java
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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class EmailContent {

  private String emailSubject;
  private String emailContents;
  private List<String> to;
  private List<String> cc;
}
