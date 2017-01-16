package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import org.apache.commons.lang3.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ExploreVkService {

    @Autowired
    VkProfileRepository vkProfileRepository

    VkDatasetExploration getVkDatasetExploration() {
        VkDatasetExploration vkDatasetExploration = new VkDatasetExploration()
        vkDatasetExploration.datasetSize = vkProfileRepository.count()

        return vkDatasetExploration
    }

    VkProfile getRandomVkProfile() {
        int datasetSize = vkProfileRepository.count()
        int randomVkProfileIndex = RandomUtils.nextInt(0, datasetSize)
        return vkProfileRepository.findAll(new PageRequest(randomVkProfileIndex, 1)).content.first()
    }

    VkProfile getVkProfileById(String id) {
        vkProfileRepository.findOne(id)
    }

    VkProfile getVkProfileByVkId(Integer vkId) {
        vkProfileRepository.findOneByVkId(vkId)
    }
}
