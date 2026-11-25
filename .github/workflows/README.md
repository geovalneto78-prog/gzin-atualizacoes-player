# GZIN ATUALIZAÇÕES PLAYER — projeto Android inicial

Este é um projeto-fonte Android Studio em Kotlin + Jetpack Compose, com identidade visual azul neon e telas separadas PLAYER, ANALYSER, EQUALIZER e CONFIG.

## Como gerar o APK
1. Instale o Android Studio (versão atual estável) em um computador.
2. Extraia este ZIP.
3. No Android Studio, escolha **Open** e selecione a pasta `GZIN_Atualizacoes_Player`.
4. Aguarde a sincronização do Gradle.
5. Acesse **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. O APK de depuração será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Estado desta entrega
Esta é uma base inicial de interface e navegação, não um APK final de produção. A seleção de pasta está preparada como ponto de integração, mas a listagem recursiva do pendrive, reprodução em segundo plano com serviço, analisador real ligado ao áudio e aplicação efetiva do equalizador Android ainda precisam ser implementados e testados em aparelhos físicos. O gráfico atual é demonstrativo e não deve ser usado para regular equipamento como se fosse medição real.

## Observações
- O acesso ao pendrive depende de USB-OTG, permissões do Android e suporte do aparelho.
- Equalizador e analisador reais variam conforme o dispositivo e o caminho de áudio.
- Não use valores do gráfico demonstrativo para ajustar amplificadores ou proteger alto-falantes.
