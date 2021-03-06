package pl.allegro.tech.build.axion.release.domain

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.ComponentFactory
import spock.lang.Specification

class ReleaserTest extends Specification {

    Project project

    ScmRepository repository

    Releaser releaser

    VersionConfig config

    def setup() {
        project = ProjectBuilder.builder().build()

        Grgit.init(dir: project.rootDir)

        config = ComponentFactory.versionConfig(project, 'scmVersion')
        repository = ComponentFactory.scmRepository(project, config)

        repository.commit('initial commit')

        releaser = new Releaser(repository, new LocalOnlyResolver(config, project), project.logger)
    }

    def "should release new version when not on tag"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', '2.0.0')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '2.0.0'
    }

    def "should not release version when on tag"() {
        given:
        repository.tag('release-1.0.0')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '1.0.0'
    }

    def "should create release commit if configured"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', '3.0.0')
        config.createReleaseCommit = true

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.0.0'
        repository.lastLogMessages(1) == ['release version: 3.0.0']
    }
}
