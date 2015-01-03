import europass.interop.demo.DbService
import europass.interop.demo.EmployeeService
import europass.interop.demo.ProfileService
import ratpack.form.Form
import ratpack.form.UploadedFile
import ratpack.groovy.sql.SqlModule
import ratpack.hikari.HikariModule

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {

    bindings {
        add new HikariModule([URL: "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV"], "org.h2.jdbcx.JdbcDataSource")
        add new SqlModule()
        bind(DbService.class)
        init { DbService dbService ->
            dbService.createTable()
        }
    }

    handlers {
        get {
            render groovyTemplate('index.html')
        }

        prefix('profile') { ProfileService profileService, DbService dbService ->
            handler('upload') {
                byMethod {
                    get() {
                        render groovyTemplate('profile/upload.html')
                    }
                    post() {
                        Form form = parse Form
                        UploadedFile file = form.file('europass-file')
                        blocking {
                            profileService.upload(file)
                        } then { long id ->
                            redirect "/profile/${id}"
                        }
                    }
                }
            }
            handler('create') {
                byMethod {
                    get() {
                        render groovyTemplate('profile/form.html')
                    }
                    post() {
                        Form form = parse Form
                        blocking {
                            dbService.save(form)
                        } then { long id ->
                            redirect "/profile/${id}"
                        }
                    }
                }
            }
            handler('update/:id') {
                def id = pathTokens['id'] as Long
                byMethod {
                    get() {
                        blocking {
                            dbService.findById(id)
                        } then { Map<String, String> profile ->
                            render groovyTemplate(profile: profile, 'profile/form.html')
                        }
                    }
                    post() {
                        Form form = parse Form
                        blocking {
                            dbService.update(id, form)
                        } then {
                            redirect "/profile/${id}"
                        }
                    }
                }
            }
            get(':id') {
                Long id = pathTokens.get('id', 1) as Long
                blocking {
                    dbService.findById(id)
                } then { Map<String, String> profile ->
                    render groovyTemplate([profile: profile], 'profile/view.html')
                }
            }
            get('/:id/pdf') {
                Long id = pathTokens.get('id', 1) as Long
                blocking {
                    profileService.exportToPdf(id)
                } then { byte[] pdfBytes ->
                    response.contentType('application/pdf;charset=utf-8')
                    //response.headers.set('Content-Disposition', 'attachment; filename=export.pdf')
                    response.headers.set('Content-Disposition', 'filename=europass.pdf')
                    response.send(pdfBytes)
                }
            }
        }

        prefix('employees') { EmployeeService employeeService ->
            get() {
                Map model = [employees: employeeService.list()]
                render groovyTemplate(model, 'employees/list.html')
            }
            get(':id') {
                int index = pathTokens['id'] as int
                Map model = [employee: employeeService.findByIndex(index)]
                render groovyTemplate(model, 'employees/view.html')
            }
        }

        assets 'public'
    }
}
