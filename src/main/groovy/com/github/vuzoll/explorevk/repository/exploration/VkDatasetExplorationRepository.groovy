package com.github.vuzoll.explorevk.repository.exploration

import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import org.springframework.data.repository.PagingAndSortingRepository

interface VkDatasetExplorationRepository extends PagingAndSortingRepository<VkDatasetExploration, String> {

    Collection<VkDatasetExploration> findByStatus(String status)
}
