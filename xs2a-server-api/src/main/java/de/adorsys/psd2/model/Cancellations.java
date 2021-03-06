/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An array of all cancellationIds.
 */
@ApiModel(description = "An array of all cancellationIds.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-16T13:49:16.891743+02:00[Europe/Kiev]")

public class Cancellations {
    @JsonProperty("cancellationIds")
    private CancellationList cancellationIds = null;

    public Cancellations cancellationIds(CancellationList cancellationIds) {
        this.cancellationIds = cancellationIds;
        return this;
    }

    /**
     * Get cancellationIds
     *
     * @return cancellationIds
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid


    @JsonProperty("cancellationIds")
    public CancellationList getCancellationIds() {
        return cancellationIds;
    }

    public void setCancellationIds(CancellationList cancellationIds) {
        this.cancellationIds = cancellationIds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cancellations cancellations = (Cancellations) o;
        return Objects.equals(this.cancellationIds, cancellations.cancellationIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cancellationIds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Cancellations {\n");

        sb.append("    cancellationIds: ").append(toIndentedString(cancellationIds)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

