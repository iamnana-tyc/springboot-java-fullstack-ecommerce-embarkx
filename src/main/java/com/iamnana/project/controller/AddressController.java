package com.iamnana.project.controller;

import com.iamnana.project.model.User;
import com.iamnana.project.payload.AddressDTO;
import com.iamnana.project.service.AddressService;
import com.iamnana.project.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address", description = "APIs for managing the address endpoints")
@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    AuthUtil authUtil;

    @Operation(summary = "Create address", description = "API to create address")
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO address = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all addresses", description = "API to get all address")
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
        List<AddressDTO> addressDTOList = addressService.getAddresses();

        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @Operation(summary = "Get a specific address", description = "API endpoint for getting an address by addressId")
    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(
            @Parameter(description = "The id of the address")
            @PathVariable Long addressId){
        AddressDTO address = addressService.getAddressById(addressId);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @Operation(summary = "Get user address", description = "API endpoint for getting user address")
    @GetMapping("user/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        User user = authUtil.loggedInUser();
        List<AddressDTO> address = addressService.getUserAddresses(user);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @Operation(summary = "Update an address", description = "API endpoint for updating address")
    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            @Parameter(description = "The id of address you wish to update")
            @PathVariable Long addressId){
        AddressDTO address = addressService.updateAddress(addressDTO,addressId);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @Operation(summary = "Delete an address", description = "API endpoint for deleting address")
    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @Parameter(description = "The id of address you wish to delete")
            @PathVariable Long addressId){
        String status = addressService.deleteAddress(addressId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
