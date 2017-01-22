package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkUniversity

class TopUniversitiesExploration extends VkDatasetExploration {

    Integer numberOfUniversitiesToTake

    Distribution<VkUniversity> universityDistribution
}
