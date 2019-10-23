// Generated on 2014-06-04 using generator-angular 0.8.0
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'
var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;
var serveStatic = require('serve-static');

module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-war');

    // Load grunt tasks automatically
    require('load-grunt-tasks')(grunt);

    // Time how long tasks take. Can help when optimizing build times
    require('time-grunt')(grunt);

    // register port option
    var port = grunt.option('port');

    // Define the configuration for all the tasks
    grunt.initConfig({

        war: {
            target: {
                options: {
                    // to
                    war_dist_folder: '<%= yeoman.dist %>',
                    war_verbose: true,
                    war_name: 'webapp',
                    webxml_welcome: 'index.html',
                    webxml_display_name: 'PatientView webapp',
                    webxml_mime_mapping: [{
                        extension: 'woff',
                        mime_type: 'application/font-woff'
                    }]
                },
                files: [
                    {
                        expand: true,
                        // from
                        cwd: '<%= yeoman.dist %>',
                        src: ['**'],
                        dest: ''
                    }
                ]
            }
        },

        secure: grunt.file.readJSON('secure.json'),

        // Project settings
        yeoman: {
            // configurable paths
            app: require('./bower.json').appPath || 'app',
            dist: 'dist'
        },

        babel: {
            options: {
                sourceMap: true,
                presets: ["@babel/preset-es2015"]
            },
            dist: {
                files: {
                'dist/app.js': 'src/app.js'
                }
            }
        },

        // Watches files for changes and runs tasks based on the changed files
        watch: {
            bower: {
                files: ['bower.json'],
                tasks: ['bowerInstall']
            },
            js: {
                files: ['<%= yeoman.app %>/scripts/{,*/}*.js'],
                tasks: ['newer:jshint:all'],
                options: {
                    livereload: true
                }
            },
            jsTest: {
                files: ['test/spec/{,*/}*.js'],
                tasks: ['newer:jshint:test', 'karma']
            },
            styles: {
                files: ['<%= yeoman.app %>/styles/{,*/}*.css'],
                tasks: ['newer:copy:styles', 'autoprefixer']
            },
            gruntfile: {
                files: ['Gruntfile.js']
            },
            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [
                    '<%= yeoman.app %>/{,*/}*.html',
                    '.tmp/styles/{,*/}*.css',
                    '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ]
            }
        },

        // The actual grunt server settings
        connect: {
            options: {
                port: 9000,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname: '0.0.0.0',
                livereload: 35729
            },
            proxies: [
                {
                    context: '/api',
                    host: 'patientview201.apiary-mock.com',
                    port: 80,
                    https: false,
                    changeOrigin: false,
                    xforward: false,
                    headers: {
                        "Host": "patientview201.apiary-mock.com"
                    },rewrite: {
                    //'^/api': ''
                }
                }
            ],
            livereload: {
                options: {

                    middleware: function (connect) {
                        return [
                            proxySnippet,
                            serveStatic(require('path').resolve('app')),
                            serveStatic(require('path').resolve('.tmp'))
                        ];
                    },
                    open: false,
                    base: [
                        '.tmp',
                        '<%= yeoman.app %>'
                    ]
                }
            },
            test: {
                options: {
                    port: 9001,
                    base: [
                        '.tmp',
                        'test',
                        '<%= yeoman.app %>'
                    ]
                }
            },
            dist: {
                options: {
                    base: '<%= yeoman.dist %>'
                }
            }
        },

        // Make sure code styles are up to par and there are no obvious mistakes
        jshint: {
            options: {
                jshintrc: '.jshintrc',
                reporter: require('jshint-stylish')
            },
            all: [
                '!Gruntfile.js',
                '<%= yeoman.app %>/scripts/{,*/}*.js'
            ],
            test: {
                options: {
                    jshintrc: 'test/.jshintrc'
                },
                src: ['test/spec/{,*/}*.js']
            }
        },

        // Empties folders to start fresh
        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },

        // Add vendor prefixed styles
        autoprefixer: {
            options: {
                browsers: ['last 1 version']
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: '.tmp/styles/',
                    src: '{,*/}*.css',
                    dest: '.tmp/styles/'
                }]
            }
        },

        // Automatically inject Bower components into the app
        bowerInstall: {
            app: {
                src: ['<%= yeoman.app %>/index.html'],
                ignorePath: '<%= yeoman.app %>/'
            }
        },

        // Renames files for browser caching purposes
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= yeoman.dist %>/scripts/{,*/}*.js'
                    ]
                }
            }
        },

        // Reads HTML for usemin blocks to enable smart builds that automatically
        // concat, minify and revision files. Creates configurations in memory so
        // additional tasks can operate on them
        useminPrepare: {
            html: '<%= yeoman.app %>/index.html',
            options: {
                dest: '<%= yeoman.dist %>',
                flow: {
                    html: {
                        steps: {
                            js: ['concat', 'uglifyjs'],
                            css: ['cssmin']
                        },
                        post: {}
                    }
                }
            }
        },

        // Performs rewrites based on rev and the useminPrepare configuration
        usemin: {
            html: ['<%= yeoman.dist %>/{,*/}*.html'],
            css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
            options: {
                assetsDirs: ['<%= yeoman.dist %>']
            }
        },

        // The following *-min tasks produce minified files in the dist folder
        cssmin: {
            options: {
                root: '<%= yeoman.app %>'
            }
        },

        imagemin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>/images',
                    src: '{,*/}*.{png,jpg,jpeg,gif}',
                    dest: '<%= yeoman.dist %>/images'
                }]
            }
        },

        svgmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>/images',
                    src: '{,*/}*.svg',
                    dest: '<%= yeoman.dist %>/images'
                }]
            }
        },

        htmlmin: {
            dist: {
                options: {
                    collapseWhitespace: true,
                    collapseBooleanAttributes: true,
                    removeCommentsFromCDATA: true,
                    removeOptionalTags: true
                },
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.dist %>',
                    src: ['*.html', 'views/{,*/}*.html', 'scripts/directives/templates/{,*/}*.html'],
                    dest: '<%= yeoman.dist %>'
                }]
            }
        },

        // ngmin tries to make the code safe for minification automatically by
        // using the Angular long form for dependency injection. It doesn't work on
        // things like resolve or inject so those have to be done manually.
        ngmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '.tmp/concat/scripts',
                    src: '*.js',
                    dest: '.tmp/concat/scripts'
                }]
            }
        },

        // Replace Google CDN references
        cdnify: {
            dist: {
                html: ['<%= yeoman.dist %>/*.html']
            }
        },

        // Copies remaining files to places other tasks can use
        copy: {
            minimal: {
                expand:true,
                cwd: '<%= yeoman.app %>',
                dest: '<%= yeoman.dist %>',
                src: [
                    '**/*'
                ]
            },
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.app %>',
                    dest: '<%= yeoman.dist %>',
                    src: [
                        '*.{ico,png,txt}',
                        '.htaccess',
                        '*.html',
                        'views/{,*/}*.html',
                        'scripts/directives/templates/{,*/}*.html',
                        'images/{,*/}*.{webp}',
                        'fonts/*',
                        'bower_components/bootstrap/dist/fonts/*.*'
                    ]
                }, {
                    expand: true,
                    cwd: '.tmp/images',
                    dest: '<%= yeoman.dist %>/images',
                    src: ['generated/*']
                }]
            },
            styles: {
                expand: true,
                cwd: '<%= yeoman.app %>/styles',
                dest: '.tmp/styles/',
                src: '{,*/}*.css'
            }
        },

        // Run some tasks in parallel to speed up the build process
        concurrent: {
            server: [
                'copy:styles'
            ],
            test: [
                'copy:styles'
            ],
            dist: [
                'copy:styles',
                'imagemin',
                'svgmin'
            ]
        },

        // By default, your `index.html`'s <!-- Usemin block --> will take care of
        // minification. These next options are pre-configured if you do not wish
        // to use the Usemin blocks.
        // cssmin: {
        //   dist: {
        //     files: {
        //       '<%= yeoman.dist %>/styles/main.css': [
        //         '.tmp/styles/{,*/}*.css',
        //         '<%= yeoman.app %>/styles/{,*/}*.css'
        //       ]
        //     }
        //   }
        // },
        // uglify: {
        //   dist: {
        //     files: {
        //       '<%= yeoman.dist %>/scripts/scripts.js': [
        //         '<%= yeoman.dist %>/scripts/scripts.js'
        //       ]
        //     }
        //   }
        // },
        // concat: {
        //   dist: {}
        // },

        // Test settings
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true
            }
        },

        // grunt-ng-constant
        // XXXprod and XXXdev are writing file to the same place, but may change
        //http://mindthecode.com/how-to-use-environment-variables-in-your-angular-application/
        ngconstant: {
            // Options for all targets
            options: {
                space: '  ',
                wrap: '"use strict";\n\n {%= __ngModule %}',
                name: 'config'
            },
            // Environment targets
            apiarydev: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'development',
                        apiEndpoint: 'http://patientview201.apiary-mock.com/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            },
            apidev: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'https://test.patientview.org/api',
                        //apiEndpoint: 'http://192.168.1.249:8080/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            },
            apilocal: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'http://localhost:' + port + '/api',
                        reCaptchaPublicKey: '6Lcrn0QUAAAAAJzzJaDrHK9_3udkFe3Xe9Cmj08m',
                        buildDateTime: Date.now()
                    }
                }
            },
            apiproduction: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: '/api',
                        reCaptchaPublicKey: '6Lcrn0QUAAAAAJzzJaDrHK9_3udkFe3Xe9Cmj08m',
                        buildDateTime: Date.now()
                    }
                }
            },
            apiievm: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'http://10.0.2.2:8080/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            },
            apiaryprod: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'development',
                        apiEndpoint: 'http://patientview201.apiary-mock.com/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            },
            apiprod: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'http://localhost:8089/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            },
            apissgdev: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'http://diabetes-pv.dev.solidstategroup.com/api',
                        reCaptchaPublicKey: '6Lcrn0QUAAAAAJzzJaDrHK9_3udkFe3Xe9Cmj08m',
                        buildDateTime: Date.now()
                    }
                }
            },
            apissgstaging: {
                options: {
                    dest: '<%= yeoman.app %>/scripts/config.js'
                },
                constants: {
                    ENV: {
                        name: 'production',
                        apiEndpoint: 'http://patientview2.staging.solidstategroup.com/api',
                        reCaptchaPublicKey: '',
                        buildDateTime: Date.now()
                    }
                }
            }
        }
    });

    grunt.registerTask('serveapi', function (target) {
        grunt.task.run([
            'clean:server',
            'ngconstant:apidev',
            'bowerInstall',
            'concurrent:server',
            'autoprefixer',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('servelocal', function (target) {
        grunt.task.run([
            'clean:server',
            'ngconstant:apilocal',
            'bowerInstall',
            'concurrent:server',
            'autoprefixer',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('test', [
        'newer:jshint',
        'clean:server',
        'concurrent:test',
        'autoprefixer',
        'connect:test',
        'karma'
    ]);

    grunt.registerTask('build', [
        'test',
        'clean:dist',
        'ngconstant:apiprod',
        'bowerInstall',
        'useminPrepare',
        'concurrent:dist',
        'autoprefixer',
        'concat',
        'ngmin',
        'copy:dist',
        'cdnify',
        'cssmin',
        'babel',
        'uglify',
        'rev',
        'usemin',
        'htmlmin',
        'war'
    ]);

    grunt.registerTask('buildlocal', [
        'clean:dist',
        //'ngconstant:apiprod',
        'ngconstant:apilocal',
        'bowerInstall',
        'useminPrepare',
        'concurrent:dist',
        'autoprefixer',
        'concat',
        'copy:dist',
        'cdnify',
        'cssmin',
        'babel',
        'uglify',
        'rev',
        'usemin',
        'htmlmin',
        'war'
    ]);

    grunt.registerTask('default', [
        'newer:jshint',
        'test',
        'build'
    ]);

    // now with minification
    grunt.registerTask('minimal', [
        'clean:dist',
        'ngconstant:apilocal',
        'copy:minimal',
        /*'useminPrepare',
        'concat',
        'uglify',
        'cssmin',
        'usemin',*/
        'war'
    ]);

    grunt.registerTask('minimalievm', [
        'clean:dist',
        'ngconstant:apiievm',
        'copy:minimal',
        'war'
    ]);

    grunt.registerTask('minimallive', [
        'clean:dist',
        'ngconstant:apiprod',
        'copy:minimal',
        'war'
    ]);

    // now with minification
    grunt.registerTask('minimalssgdev', [
        'clean:dist',
        'ngconstant:apissgdev',
        'copy:minimal',
        'useminPrepare',
        'concat',
        'uglify',
        'cssmin',
        'usemin',
        'war'
    ]);

    grunt.registerTask('minimalssgproduction_nowar', [
        'clean:dist',
        'ngconstant:apiproduction',
        'copy:minimal',
        'useminPrepare',
        'concat',
        'babel',
        'uglify',
        'cssmin',
        'rev',
        'usemin'
    ]);
    grunt.registerTask('minimalssgdev_nowar', [
        'clean:dist',
        'ngconstant:apidev',
        'copy:minimal',
        'useminPrepare',
        'concat',
        'babel',
        'uglify',
        'cssmin',
        'rev',
        'usemin'
    ]);

    grunt.registerTask('minimalssgstaging', [
        'clean:dist',
        'ngconstant:apissgstaging',
        'copy:minimal',
        'war'
    ]);
};

