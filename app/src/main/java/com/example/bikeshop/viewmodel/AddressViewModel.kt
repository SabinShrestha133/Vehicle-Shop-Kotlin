package com.example.bikeshop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bikeshop.model.AddressModel
import com.example.bikeshop.repository.AddressRepository

class AddressViewModel(private val repository: AddressRepository) : ViewModel() {

    fun addAddress(address: AddressModel, callback: (Boolean, String) -> Unit) {
        repository.addAddress(address, callback)
    }

    fun getAddresses(userId: String, callback: (List<AddressModel>) -> Unit) {
        repository.getAddresses(userId, callback)
    }

    fun updateAddress(address: AddressModel, callback: (Boolean, String) -> Unit) {
        repository.updateAddress(address, callback)
    }

    fun deleteAddress(addressId: String, callback: (Boolean, String) -> Unit) {
        repository.deleteAddress(addressId, callback)
    }
}
