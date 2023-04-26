import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependency

// shortcut for workaround
fun PluginDependenciesSpec.id(
    application: Provider<PluginDependency>
) {
    id(application.get().pluginId)
}
