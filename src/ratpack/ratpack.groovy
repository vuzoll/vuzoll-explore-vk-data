import groovy.json.JsonSlurper
import ratpack.http.MutableHeaders

import static ratpack.groovy.Groovy.ratpack

ratpack {
    handlers {

        get('datasets/vk-by-vlad/record/random') {
            MutableHeaders headers = response.headers
            headers.set('Access-Control-Allow-Origin', '*')
            headers.set('Access-Control-Allow-Headers', 'x-requested-with, origin, content-type, accept')
            headers.set('content-type', 'application/json')

            String inputFile = '/data/vk.data'

            File dataFile = new File(inputFile)
            String dataAsText = "[ ${dataFile.text.split('\n').join(', ')} ]"
            def dataAsJson = new JsonSlurper().parseText(dataAsText)

            def randomId = new Random().nextInt(dataAsJson.size())
            def randomRecord = dataAsJson.get(randomId)

            render """{
                "id": ${randomId},
                "vkid": ${randomRecord.user_id},
                "country": "id:${randomRecord.country}",
                "city": "id:${randomRecord.city}",
                "education" : ${randomRecord.universities.collect( { '{ "faculty": "' + it.faculty_name + '", "university": "' + it.name + '", "city": "id:' + it.city + '", "country": "id:' + it.country + '" }' })}, 
                "career" : ${randomRecord.career.collect( { '{ "position": "' + it.position + '", "company": "' + it.company + '", "city": "id:' + it.city_id + '", "country": "id:' + it.country_id + '" }' })}, 
                "raw": "${randomRecord}"
            }"""
        }

        get('datasets/vk-by-vlad/record/:id') {
            MutableHeaders headers = response.headers
            headers.set('Access-Control-Allow-Origin', '*')
            headers.set('Access-Control-Allow-Headers', 'x-requested-with, origin, content-type, accept')
            headers.set('content-type', 'application/json')

            String inputFile = '/data/vk.data'

            File dataFile = new File(inputFile)
            String dataAsText = "[ ${dataFile.text.split('\n').join(', ')} ]"
            def dataAsJson = new JsonSlurper().parseText(dataAsText)

            def id = Integer.parseInt(pathTokens.id)
            def record = dataAsJson.get(id)

            render """{
                "id": ${id},
                "vkid": ${record.user_id},
                "country": "id:${record.country}",
                "city": "id:${record.city}",
                "education" : ${record.universities.collect( { '{ "faculty": "' + it.faculty_name + '", "university": "' + it.name + '", "city": "id:' + it.city + '", "country": "id:' + it.country + '" }' })}, 
                "career" : ${record.career.collect( { '{ "position": "' + it.position + '", "company": "' + it.company + '", "city": "id:' + it.city_id + '", "country": "id:' + it.country_id + '" }' })}, 
                "raw": "${record}"
            }"""
        }

        post('datasets/vk-by-vlad/explore/generate-for-aus') {
            String inputFile = '/data/vk.data'
            String outputFile = "/data/aus-data-$System.currentTimeMillis().csv"

            File dataFile = new File(inputFile)
            String dataAsText = "[ ${dataFile.text.split('\n').join(', ')} ]"
            def dataAsJson = new JsonSlurper().parseText(dataAsText)

            String ausData = 'city_id,graduation_year,university_id,faculty_id\n' + dataAsJson.collect( { "${it.city},${it.graduation},${it.university},${it.faculty}" } ).join('\n')

            new File(outputFile).text = ausData

            render "done @ $outputFile"
        }

        get('datasets/vk-by-vlad/explore') {
            MutableHeaders headers = response.headers
            headers.set('Access-Control-Allow-Origin', '*')
            headers.set('Access-Control-Allow-Headers', 'x-requested-with, origin, content-type, accept')
            headers.set('content-type', 'application/json')

            String inputFile = '/data/vk.data'

            File dataFile = new File(inputFile)
            String dataAsText = "[ ${dataFile.text.split('\n').join(', ')} ]"
            def dataAsJson = new JsonSlurper().parseText(dataAsText)

            render """{
                "size": {
                  "total": ${dataAsJson.size()},
                  "unique": ${dataAsJson.collect({ it.user_id }).unique().size()}
                },
                "country": {
                  "count": ${dataAsJson.collect({ it.country }).findAll({it > 0}).unique().size()},
                  "undefined": ${dataAsJson.collect({ it.country }).findAll({it == 0}).size()},
                  "distribution": ${dataAsJson.collect({ it.country }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "id:' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                },
                "city": {
                  "count": ${dataAsJson.collect({ it.city }).findAll({it > 0}).unique().size()},
                  "undefined": ${dataAsJson.collect({ it.cities }).findAll({it == 0}).size()},
                  "distribution": ${dataAsJson.collect({ it.city }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "id:' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                },
                "university": {
                  "count": ${dataAsJson.collect({ it.university_name }).findAll({it != ''}).unique().size()},
                  "undefined": ${dataAsJson.collect({ it.university_name }).findAll({it == ''}).size()},
                  "distribution": ${dataAsJson.collect({ it.university_name }).findAll({it != ''}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                },
                "faculty": {
                  "count": ${dataAsJson.collect({ it.faculty_name }).findAll({it != ''}).unique().size()},
                  "undefined": ${dataAsJson.collect({ it.faculty_name }).findAll({it == ''}).size()},
                  "distribution": ${dataAsJson.findAll({it.faculty_name != ''}).collect({ "${it.faculty_name} ${it.university_name}".trim() }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                },
                "graduateYear": {
                  "count": ${dataAsJson.collect({ it.graduation }).findAll({it > 0}).unique().size()},
                  "undefined": ${dataAsJson.collect({ it.graduation }).findAll({it == 0}).size()},
                  "distribution": ${dataAsJson.collect({ it.graduation }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                },
                "occupation": {
                  "type": {
                    "count": ${dataAsJson.collect({ it?.occupation?.type }).unique().size()},
                    "distribution": ${dataAsJson.collect({ it?.occupation?.type }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  },
                  "name": {
                    "count": ${dataAsJson.collect({ it?.occupation?.name }).unique().size()},
                    "distribution": ${dataAsJson.collect({ it?.occupation?.name }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  }
                },
                "work": {
                  "name": {
                    "count": ${dataAsJson.findAll({ it?.occupation?.type == 'work' && !it?.occupation?.name?.contains('Эта страница официально подтверждена') }).collect({ it?.occupation?.name }).unique().size()},
                    "distribution": ${dataAsJson.findAll({ it?.occupation?.type == 'work' && !it?.occupation?.name?.contains('Эта страница официально подтверждена') }).collect({ it?.occupation?.name }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  }
                },
                "career": {
                  "size": {
                    "distribution": ${dataAsJson.collect({ it.career.size() }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  },
                  "company": {
                    "count": ${dataAsJson.collect({ it.career.collect({it.company}) }).flatten().findAll({it != null}).unique().size()},
                    "distribution": ${dataAsJson.collect({ it.career.collect({it.company}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  },
                  "city": {
                    "count": ${dataAsJson.collect({ it.career.collect({it.city_id}) }).flatten().findAll({it != null}).unique().size()},
                    "distribution": ${dataAsJson.collect({ it.career.collect({it.city_id}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  },
                  "position": {
                    "count": ${dataAsJson.collect({ it.career.collect({it.position?.toLowerCase()}) }).flatten().findAll({it != null}).unique().size()},
                    "distribution": ${dataAsJson.collect({ it.career.collect({it.position?.toLowerCase()}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
                  }
                }
            }"""
        }
    }
}