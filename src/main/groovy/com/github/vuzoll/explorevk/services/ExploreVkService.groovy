package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExploreVkService {

    @Autowired
    VkProfileRepository vkProfileRepository

    VkExploration explore() {
        VkExploration vkExploration = new VkExploration()
        vkExploration.datasetSize = vkProfileRepository.count()

        return vkExploration
    }
}
