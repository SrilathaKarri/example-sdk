package sdk.facility.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonTypeName("AddAddressProof")
public class AddressProof {

    @NotNull
    @NotBlank(message = "Address Proof type is required")
    private String addressProofType;

    @NotNull
    private Document addressProofAttachment;

}

