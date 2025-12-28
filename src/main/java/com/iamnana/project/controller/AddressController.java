package com.iamnana.project.controller;

import com.iamnana.project.model.User;
import com.iamnana.project.payload.AddressDTO;
import com.iamnana.project.service.AddressService;
import com.iamnana.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO address = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
        List<AddressDTO> addressDTOList = addressService.getAddresses();

        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){
        AddressDTO address = addressService.getAddressById(addressId);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("user/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        User user = authUtil.loggedInUser();
        List<AddressDTO> address = addressService.getUserAddresses(user);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }
}
