package com.github.vuzoll.explorevk.repository.vk

import com.github.vuzoll.explorevk.domain.vk.VkProfile
import org.springframework.data.repository.PagingAndSortingRepository

interface VkProfileRepository extends PagingAndSortingRepository<VkProfile, String> {

    VkProfile findOneByVkId(Integer vkId)
}