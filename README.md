# Qual é a Boa? — Mobile

Aplicativo Android para descobrir e divulgar eventos ("boas") da cidade.
Feito em **Kotlin** com **ViewBinding**, **ConstraintLayout** e **RecyclerView**.

## Como abrir e executar
1. No Android Studio, abra a pasta `QualEABoaMobile` (File → Open).
2. Aguarde a sincronização do Gradle.
3. Escolha um emulador ou celular com Android 7.0 (API 24) ou superior e clique em **Run**.

Dica para testar fotos no emulador: abra o navegador do emulador, salve uma imagem e depois escolha-a na tela de cadastro.

## O que já funciona
- Lista de eventos (RecyclerView) com foto, nome, data e local, e busca por nome ou local.
- Criar, editar e apagar eventos, com foto escolhida na galeria (ActivityResult).
- Tela de detalhes (Intent explícita) com **Ver no mapa** e **Salvar na agenda** (Intents implícitas).
- Textos em português (pt-BR) e inglês, escolhidos pelo idioma do celular.
- Tema claro e escuro, seguindo a configuração do celular.

## Em andamento
- Por enquanto os eventos ficam **só na memória** (somem ao fechar o app). O banco de dados (Room) entra na próxima etapa.
- Depois: login, cadastro de usuários e "Tô dentro!".
