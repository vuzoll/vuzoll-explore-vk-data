package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.services.ExplorationService
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class ExploreVkDatasetController {

    @Autowired
    ExplorationService explorationService

    @PostMapping(path = '/explore/current-location')
    @ResponseBody VkDatasetExploration exploreCurrentLocation() {
        log.info "Receive 'current location' exploration request"

        VkDatasetExploration currentlyRunningExploration = explorationService.getCurrentlyRunningExploration()
        if (currentlyRunningExploration != null) {
            log.error "Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one"
            throw new IllegalStateException("Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one")
        }

        return explorationService.startNewCurrentLocationExploration()
    }

    @PostMapping(path = '/explore/top-faculties/{numberOfFacultiesToTake}')
    @ResponseBody VkDatasetExploration exploreTopFaculties(@PathVariable Integer numberOfFacultiesToTake) {
        log.info "Receive 'top faculties' exploration request"

        VkDatasetExploration currentlyRunningExploration = explorationService.getCurrentlyRunningExploration()
        if (currentlyRunningExploration != null) {
            log.error "Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one"
            throw new IllegalStateException("Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one")
        }

        return explorationService.startNewTopFacultiesExploration(numberOfFacultiesToTake)
    }

    @GetMapping(path = '/exploration/{explorationId}')
    @ResponseBody VkDatasetExploration explorationStatus(@PathVariable String explorationId) {
        explorationService.explorationStatus(explorationId)
    }

    @GetMapping(path = '/exploration/last')
    @ResponseBody VkDatasetExploration lastExplorationStatus() {
        explorationService.getLastExploration()
    }

    @GetMapping(path = '/exploration/all')
    @ResponseBody List<VkDatasetExploration> allExplorations() {
        explorationService.getAllExplorations()
    }
}
