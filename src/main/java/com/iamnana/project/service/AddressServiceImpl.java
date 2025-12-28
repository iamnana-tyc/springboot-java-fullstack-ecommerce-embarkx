package com.iamnana.project.service;

import com.iamnana.project.exceptions.ResourceNotFoundException;
import com.iamnana.project.model.Address;
import com.iamnana.project.model.User;
import com.iamnana.project.payload.AddressDTO;
import com.iamnana.project.respositories.AddressRepository;
import com.iamnana.project.respositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);

        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addressDTOList =  addressRepository.findAll();

        return addressDTOList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addressList = user.getAddresses();

        return addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address existingAddressInDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        existingAddressInDatabase.setStreet(addressDTO.getStreet());
        existingAddressInDatabase.setBuildingName(addressDTO.getBuildingName());
        existingAddressInDatabase.setCity(addressDTO.getCity());
        existingAddressInDatabase.setState(addressDTO.getState());
        existingAddressInDatabase.setZipCode(addressDTO.getZipCode());
        existingAddressInDatabase.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(existingAddressInDatabase);

        //update address in user class
        User user = existingAddressInDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address existingAddressInDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = existingAddressInDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));

        addressRepository.delete(existingAddressInDatabase);

        return "Address deleted successfully with id: " + addressId;
    }
}
