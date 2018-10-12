package nl.lemkes.example.processing

import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.ClassPathResource

fun main(args: Array<String>) {

    val howtouse= "Expected: the next arguments: " +
            "java -jar featured-to-extract-cli.jar " +
            "[ARGS]"
    val options = mutableMapOf<String, Any>()
    val arguments = mutableListOf<String>()
    inspectArgs(args, options, arguments)


    fun <T> withBean(type: Class<T>, block: (T) -> Unit) {

        val cmdProperties = MapPropertySource("options", options)

        val yamlLoader = YamlPropertySourceLoader()
        val applicationYml = ClassPathResource("application.yml")
        val yamlProperties = yamlLoader.load("application.yml", applicationYml)

        val environment = object: StandardEnvironment() {

            override fun customizePropertySources(propertySources: MutablePropertySources) {
                super.customizePropertySources(propertySources)
                yamlProperties.forEach{
                    propertySources.addLast(it)
                }
                propertySources.addFirst(cmdProperties)
            }
        }

        AnnotationConfigApplicationContext().use {
            it.environment = environment
            environment.setActiveProfiles("cli")

            it.scan("nl.lemkes.example.processing")
            it.refresh()

            block(it.getBean(type))
        }
    }

}

fun inspectArgs(args: Array<String>, options:MutableMap<String, Any>, arguments:MutableList<String>)
{
    for (arg in args) {
        if (arg.startsWith("--")) {
            val idx = arg.indexOf('=')
            if (idx == -1) {
                options[arg.substring(2)] = true
            } else {
                options[arg.substring(2, idx)] = arg.substring(idx + 1)
            }
        } else {
            arguments.add(arg)
        }
    }
}