package com.ecliptic.ui

object DevIconMapper {

    private val slugMap = mapOf(
        // JS Frameworks
        "react"                 to "react",
        "react dom"             to "react",
        "next.js"               to "nextjs",
        "nuxt.js"               to "nuxtjs",
        "vue.js"                to "vuejs",
        "angular"               to "angularjs",
        "angular 2"             to "angularjs",
        "gatsby"                to "gatsby",
        "svelte"                to "svelte",
        "sveltekit"             to "svelte",
        "ember.js"              to "ember",
        "backbone.js"           to "backbonejs",
        "alpine.js"             to "alpinejs",
        "jquery"                to "jquery",
        "jquery ui"             to "jquery",
        "jquery migrate"        to "jquery",
        "htmx"                  to "html5",
        // CMS
        "wordpress"             to "wordpress",
        "drupal"                to "drupal",
        "joomla"                to "joomla",
        "ghost"                 to "ghost",
        "contentful"            to "contentful",
        "craft cms"             to "craftsupported",
        "typo3"                 to "typo3",
        // E-commerce
        "shopify"               to "shopify",
        "woocommerce"           to "woocommerce",
        "magento"               to "magento",
        "prestashop"            to "prestashop",
        // Languages
        "php"                   to "php",
        "python"                to "python",
        "ruby"                  to "ruby",
        "java"                  to "java",
        "go"                    to "go",
        "rust"                  to "rust",
        "c#"                    to "csharp",
        "typescript"            to "typescript",
        "javascript"            to "javascript",
        "perl"                  to "perl",
        "scala"                 to "scala",
        "kotlin"                to "kotlin",
        "swift"                 to "swift",
        "dart"                  to "dart",
        // Backends / Frameworks
        "node.js"               to "nodejs",
        "express"               to "express",
        "django"                to "django",
        "ruby on rails"         to "rails",
        "laravel"               to "laravel",
        "symfony"               to "symfony",
        "codeigniter"           to "codeigniter",
        "flask"                 to "flask",
        "fastapi"               to "fastapi",
        "spring"                to "spring",
        "asp.net"               to "dotnetcore",
        ".net"                  to "dotnetcore",
        "asp.net mvc"           to "dotnetcore",
        "yii"                   to "yii",
        "cake php"              to "cakephp",
        "strapi"                to "strapi",
        // CSS / UI
        "bootstrap"             to "bootstrap",
        "tailwind css"          to "tailwindcss",
        "sass"                  to "sass",
        "less"                  to "less",
        "bulma"                 to "bulma",
        "foundation"            to "foundation",
        "material-ui"           to "materialui",
        "mui"                   to "materialui",
        "chakra ui"             to "chakraui",
        // Build tools
        "webpack"               to "webpack",
        "vite"                  to "vitejs",
        "babel"                 to "babel",
        "gulp"                  to "gulp",
        "grunt"                 to "grunt",
        "rollup.js"             to "rollupjs",
        // Databases
        "mysql"                 to "mysql",
        "postgresql"            to "postgresql",
        "mongodb"               to "mongodb",
        "redis"                 to "redis",
        "elasticsearch"         to "elasticsearch",
        "sqlite"                to "sqlite",
        "mariadb"               to "mariadb",
        "neo4j"                 to "neo4j",
        "cassandra"             to "cassandra",
        "oracle"                to "oracle",
        "microsoft sql server"  to "microsoftsqlserver",
        // Servers / DevOps
        "nginx"                 to "nginx",
        "apache"                to "apache",
        "apache http server"    to "apache",
        "docker"                to "docker",
        "kubernetes"            to "kubernetes",
        "jenkins"               to "jenkins",
        "travis ci"             to "travisci",
        "circleci"              to "circleci",
        "github actions"        to "github",
        // Cloud / Hosting
        "amazon web services"   to "amazonwebservices",
        "aws"                   to "amazonwebservices",
        "amazon s3"             to "amazonwebservices",
        "amazon cloudfront"     to "amazonwebservices",
        "google cloud"          to "googlecloud",
        "azure"                 to "azure",
        "microsoft azure"       to "azure",
        "cloudflare"            to "cloudflare",
        "vercel"                to "vercel",
        "netlify"               to "netlify",
        "heroku"                to "heroku",
        "digitalocean"          to "digitalocean",
        "firebase"              to "firebase",
        "supabase"              to "supabase",
        // Analytics / Marketing (use generic icons)
        "google analytics"      to "google",
        "google tag manager"    to "google",
        "google fonts"          to "google",
        "google maps"           to "google",
        "gitlab"                to "gitlab",
        "github"                to "github",
        // Mobile
        "flutter"               to "flutter",
        "react native"          to "react",
        "ionic"                 to "ionic",
        "capacitor"             to "capacitorjs",
        // Other
        "graphql"               to "graphql",
        "git"                   to "git",
        "npm"                   to "npm",
        "yarn"                  to "yarn",
        "prisma"                to "prisma",
        "jest"                  to "jest",
        "mocha"                 to "mocha",
        "eslint"                to "eslint",
        "html5"                 to "html5",
        "css"                   to "css3",
        "ubuntu"                to "ubuntu",
        "linux"                 to "linux",
        "centos"                to "centos",
        "debian"                to "debian",
        "fedora"                to "fedora",
        "windows server"        to "windows8",
        "nginx unit"            to "nginx",
        "apache tomcat"         to "tomcat",
        "stripe"                to "stripe",
    )

    fun slugFor(techName: String): String? = slugMap[techName.trim().lowercase()]

    fun iconUrl(slug: String): String =
        "https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/$slug/$slug-original.svg"

    // ── Simple Icons fallback ──────────────────────────────────────────────────
    // devicon is a dev-tool icon set — it lacks many brand/product logos (Shopify,
    // Stripe, social platforms, credit cards) and for some only ships a "-plain"
    // variant that our "-original" URL misses. For those we fall back to Simple
    // Icons' colored CDN (cdn.simpleicons.org), which has ~3000 verified brands.
    // Slugs below were checked against the Simple Icons slug table.
    private val simpleIconSlugs = mapOf(
        // E-commerce / CMS brands devicon lacks (or only has -plain)
        "shopify"               to "shopify",
        "woocommerce"           to "woocommerce",
        "prestashop"            to "prestashop",
        "ghost"                 to "ghost",
        "contentful"            to "contentful",
        "strapi"                to "strapi",
        // Payments
        "stripe"                to "stripe",
        "paypal"                to "paypal",
        "klarna"                to "klarna",
        "square"                to "square",
        "google pay"            to "googlepay",
        "apple pay"             to "applepay",
        // Credit-card brands (grouped tech has no icon; individual ones do)
        "visa"                  to "visa",
        "mastercard"            to "mastercard",
        "american express"      to "americanexpress",
        "discover"              to "discover",
        "jcb"                   to "jcb",
        "diners club"           to "dinersclub",
        // Social platforms (used in the Social Links sections)
        "twitter / x"           to "x",
        "twitter"               to "x",
        "x"                     to "x",
        "facebook"              to "facebook",
        "instagram"             to "instagram",
        "youtube"               to "youtube",
        "tiktok"                to "tiktok",
        "pinterest"             to "pinterest",
        "discord"               to "discord",
        "whatsapp"              to "whatsapp",
        "telegram"              to "telegram",
        // LinkedIn & Magento are absent from Simple Icons (trademark/removed) → no icon.
    )

    private fun simpleIconUrl(slug: String) = "https://cdn.simpleicons.org/$slug"

    /**
     * Resolve the best icon URL for a technology / platform name, or null if we have
     * no icon. Simple Icons fills the gaps devicon can't; devicon keeps its full-color
     * logos for everything it reliably has.
     */
    fun iconUrlFor(name: String): String? {
        val key = name.trim().lowercase()
        simpleIconSlugs[key]?.let { return simpleIconUrl(it) }
        slugMap[key]?.let { return iconUrl(it) }
        return null
    }
}
