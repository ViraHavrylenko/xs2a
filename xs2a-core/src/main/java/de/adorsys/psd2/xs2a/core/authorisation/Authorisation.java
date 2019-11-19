/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.core.authorisation;

import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@ApiModel(description = "Authorisation object", value = "Authorisation")
public class Authorisation {
    @NotNull
    @ApiModelProperty(value = "ID of the authorisation", required = true, example = "6dc3d5b3-5023-7848-3853-f7200a64e80d")
    private String id;

    @NotNull
    @ApiModelProperty(value = "Status of the authorisation", required = true)
    private ScaStatus scaStatus;

    @ApiModelProperty(value = "Corresponding PSU")
    private PsuIdData psuData;

    private PaymentAuthorisationType authorizationType;

}
