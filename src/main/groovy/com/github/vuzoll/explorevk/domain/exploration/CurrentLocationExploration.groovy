package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkCity
import com.github.vuzoll.explorevk.domain.vk.VkCountry

class CurrentLocationExploration extends VkDatasetExploration {

    Distribution<VkCountry> countryDistribution
    Distribution<VkCity> cityDistribution
}
