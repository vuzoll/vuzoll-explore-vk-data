package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkCountry

class VkDatasetExploration {

    Integer datasetSize

    List<DistributionEntry<VkCountry>> countriesDistribution
}
