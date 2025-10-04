plugins {
    id("framefork.java")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-62")))

    api(libs.hibernate.orm.v62)
    api(libs.hypersistence.utils.hibernate62)
}
