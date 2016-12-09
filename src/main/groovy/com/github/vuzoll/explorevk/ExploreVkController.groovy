package com.github.vuzoll.explorevk

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.*

import java.util.concurrent.TimeUnit

@RestController
@Slf4j
class ExploreVkController {

    static String DATA_FILE_PATH = System.getenv('EXPLORE_VK_DATA_FILE_PATH') ?: '/data/vk.data'
    static String EXPLORATION_DIRECTORY_PATH = System.getenv('EXPLORE_VK_EXPLORATION_DIRECTORY_PATH') ?: '/data'
    static String UNIV_VS_COUNTRY_FILE_NAME = System.getenv('EXPLORE_VK_UNIV_VS_COUNTRY_FILE_NAME') ?: 'univ-vs-country.csv'
    static String UNIVERSITIES_FILE_NAME = System.getenv('EXPLORE_VK_UNIVERSITIES_FILE_NAME') ?: 'universities.csv'
    static String FACULTIES_FILE_NAME = System.getenv('EXPLORE_VK_FACULTIES_FILE_NAME') ?: 'faculties.csv'
    static String COUNTRIES_FILE_NAME = System.getenv('EXPLORE_VK_COUNTRIES_FILE_NAME') ?: 'countries.csv'
    static String CITIES_FILE_NAME = System.getenv('EXPLORE_VK_CITIES_FILE_NAME') ?: 'cities.csv'

    JsonSlurper jsonSlurper = new JsonSlurper()
    File dataFile = new File(DATA_FILE_PATH)
    File univVsCountryFile = new File("$EXPLORATION_DIRECTORY_PATH/$UNIV_VS_COUNTRY_FILE_NAME")
    File universitiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$UNIVERSITIES_FILE_NAME")
    File facultiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$FACULTIES_FILE_NAME")
    File countriesFile = new File("$EXPLORATION_DIRECTORY_PATH/$COUNTRIES_FILE_NAME")
    File citiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$CITIES_FILE_NAME")

    @RequestMapping(path = '/explore', method = RequestMethod.POST)
    @ResponseBody ExploreResponse explore() {
        log.info 'Generating exploration data...'

        long startTime = System.currentTimeMillis()
        int dataSetSize = 0
        int nonEmptyEducationRecords = 0
        int univVsCountrySize = 0

        Set<University> universities = []
        Set<Faculty> faculties = []
        Set<Country> countries = []
        Set<City> cities = []

        log.info "Generating $univVsCountryFile.path..."
        if (univVsCountryFile.exists()) {
            log.warn "Removing existant $univVsCountryFile.path..."
            univVsCountryFile.delete()
        }
        univVsCountryFile.createNewFile()
        univVsCountryFile.text = 'university_id,faculty_id,country_id,city_id\n'
        dataFile.eachLine { String line ->
            VkProfile profile = new VkProfile(jsonSlurper.parseText(line))

            boolean isNotEmptyEducation = false

            profile.educationRecords.collect({ new EducationRecord(it) }).each { EducationRecord educationRecord ->
                univVsCountryFile.append "${educationRecord?.university?.vkId?:0},${educationRecord?.faculty?.vkId?:0},${profile?.country?.vkId?:0},${profile?.city?.vkId?:0}\n"
                isNotEmptyEducation = true
                univVsCountrySize++
            }

            if (isNotEmptyEducation) {
                log.info "Get at least one education record from profile id:$profile.vkId"

                nonEmptyEducationRecords++

                universities += profile.educationRecords.university
                faculties += profile.educationRecords.faculty
                countries += profile.country
                cities += profile.city
            }

            dataSetSize++
        }

        log.info "Generating $universitiesFile.path..."
        if (universitiesFile.exists()) {
            log.warn "Removing existant $universitiesFile.path..."
            universitiesFile.delete()
        }
        universitiesFile.createNewFile()
        universitiesFile.text = 'id,name\n'
        universities.findAll({ it != null }).collect({ new University(it) }).sort { it.vkId }.each { University university ->
            universitiesFile.append """${university.vkId},"${university.name}"\n"""
        }

        log.info "Generating $facultiesFile.path..."
        if (facultiesFile.exists()) {
            log.warn "Removing existant $facultiesFile.path..."
            facultiesFile.delete()
        }
        facultiesFile.createNewFile()
        facultiesFile.text = 'id,name,university_id,university_name\n'
        faculties.findAll({ it != null }).collect({ new Faculty(it) }).sort { it.vkId }.each { Faculty faculty ->
            facultiesFile.append """${faculty.vkId},"${faculty.name}",${faculty.university.vkId},"${faculty.university.name}"\n"""
        }

        log.info "Generating $countriesFile.path..."
        if (countriesFile.exists()) {
            log.warn "Removing existant $countriesFile.path..."
            countriesFile.delete()
        }
        countriesFile.createNewFile()
        countriesFile.text = 'id,name\n'
        countries.findAll({ it != null }).sort { it.vkId }.each { Country country ->
            countriesFile.append """${country.vkId},"${country.name}"\n"""
        }

        log.info "Generating $citiesFile.path..."
        if (citiesFile.exists()) {
            log.warn "Removing existant $citiesFile.path..."
            citiesFile.delete()
        }
        citiesFile.createNewFile()
        citiesFile.text = 'id,name,country_id,country_name\n'
        cities.findAll({ it != null }).sort { it.vkId }.each { City city ->
            citiesFile.append """${city.vkId},"${city.name}",${city?.country?.vkId?:0},"${city?.country?.name}"\n"""
        }

        return new ExploreResponse(timeTaken: TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS), dataSetSize: dataSetSize, nonEmptyEducationRecords: nonEmptyEducationRecords, univVsCountrySize: univVsCountrySize)
    }
}
