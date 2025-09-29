pipeline {
  agent any
  options { timestamps() }

  environment {
    BRANCH       = 'develop/backend'                                  // 트리거 브랜치
    SUBDIR       = 'chakeng'                                          // 빌드/테스트 수행 폴더
    REPO_URL     = 'https://lab.ssafy.com/s13-ai-image-sub1/S13P21D202.git'

    DEPLOY_HOST  = 'ubuntu@j13d202.p.ssafy.io'
    DEPLOY_DIR   = '/home/ubuntu/S13P21D202/chakeng'                          // 서버에 클론되어 있는 작업 디렉터리
    COMPOSE_FILE = 'chakeng/docker-compose.yml'                        // 서버에서 사용할 compose 파일 경로(DEPLOY_DIR 기준)
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test (local)') {
      when { anyOf { branch 'develop/backend'; expression { env.GIT_BRANCH == 'origin/develop/backend' } } }
      steps {
        dir("${env.SUBDIR}") {
          sh 'chmod +x gradlew || true'
          sh './gradlew clean test --no-daemon'      // 필요시 build 로 변경
        }
      }
      post {
        always {
          junit '**/build/test-results/test/*.xml'  // ← 안전 패턴
          archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true, onlyIfSuccessful: false
        }
      }
    }

    stage('Deploy (ssh → git pull → compose up)') {
      when { branch 'develop/backend' }
      steps {
        // Jenkins에 "SSH Agent" 플러그인 + 크리덴셜(ID: ec2-prod) 등록되어 있어야 합니다.
        sshagent(credentials: ['ec2-prod']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${DEPLOY_HOST} '
              set -euo pipefail


              mkdir -p ${DEPLOY_DIR}
              cd ${DEPLOY_DIR}


              if [ ! -d .git ]; then
                git clone --depth 1 -b ${BRANCH} ${REPO_URL} .
              else
                git fetch --all --prune
              fi
              git checkout ${BRANCH}
              git pull --ff-only origin ${BRANCH}


              if [ -f .gitmodules ]; then
                git submodule sync --recursive
                git submodule update --init --recursive
              fi


              if docker compose version >/dev/null 2>&1; then
                DC="docker compose"
              else
                DC="docker-compose"
              fi


              \${DC} -f ${COMPOSE_FILE} pull || true
              \${DC} -f ${COMPOSE_FILE} build --pull
              \${DC} -f ${COMPOSE_FILE} up -d --remove-orphans
              \${DC} -f ${COMPOSE_FILE} ps
            '
          """
        }
      }
    }
  }

  post {
    success { echo "✅ Deploy OK to ${env.DEPLOY_HOST} (${env.COMPOSE_FILE})" }
    failure { echo "❌ Build/Deploy failed" }
  }
}
