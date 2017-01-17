package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.exploration.ExplorationStatus
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import com.github.vuzoll.explorevk.repository.exploration.VkDatasetExplorationRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
@Slf4j
class ExplorationService {
    
    @Autowired
    ExploreVkDatasetService exploreVkDatasetService

    @Autowired
    VkDatasetExplorationRepository vkDatasetExplorationRepository

    @Autowired
    TaskExecutor taskExecutor

    @PostConstruct
    void markAbortedExplorations() {
        log.info 'Marking all aborted explorations...'
        Collection<VkDatasetExploration> abortedExplorations = vkDatasetExplorationRepository.findByStatus(ExplorationStatus.RUNNING.toString()) + vkDatasetExplorationRepository.findByStatus(ExplorationStatus.STOPPING.toString())
        if (abortedExplorations.empty) {
            log.info 'Found no aborted explorations'
        } else {
            log.warn "Found ${abortedExplorations.size()} aborted explorations"
            abortedExplorations.each { it.status = ExplorationStatus.ABORTED.toString() }
            vkDatasetExplorationRepository.save(abortedExplorations)
        }
    }
    
    VkDatasetExploration getCurrentlyRunningExploration() {
        Collection<VkDatasetExploration> currentlyRunningExplorations = vkDatasetExplorationRepository.findByStatus(ExplorationStatus.RUNNING.toString()) + vkDatasetExplorationRepository.findByStatus(ExplorationStatus.STOPPING.toString())

        if (currentlyRunningExplorations.empty) {
            return null
        }

        if (currentlyRunningExplorations.size() > 1) {
            log.error("There are more than one running exploration: ${currentlyRunningExplorations}")
            throw new IllegalStateException("There are more than one running exploration: ${currentlyRunningExplorations}")
        }

        return currentlyRunningExplorations.first()
    }

    VkDatasetExploration startNewExploration() {
        VkDatasetExploration vkDatasetExploration = new VkDatasetExploration()
        vkDatasetExploration.status = ExplorationStatus.RUNNING.toString()

        vkDatasetExploration = vkDatasetExplorationRepository.save vkDatasetExploration

        taskExecutor.execute({ exploreVkDatasetService.getVkDatasetExploration(vkDatasetExploration) })

        return vkDatasetExploration
    }

    VkDatasetExploration explorationStatus(String id) {
        vkDatasetExplorationRepository.findOne(id)
    }

    VkDatasetExploration getLastExploration() {
        List<VkDatasetExploration> allExplorations = vkDatasetExplorationRepository.findAll(new Sort(Sort.Direction.DESC, 'startTimestamp'))
        if (allExplorations.empty) {
            return null
        } else {
            return allExplorations.first()
        }
    }
}
