# Qual é a Boa? — Mobile

Aplicativo Android para descobrir e divulgar eventos ("boas") da cidade.
Feito em **Kotlin** com **ViewBinding**, **ConstraintLayout**, **RecyclerView** e **Room**.

## Como abrir e executar
1. No Android Studio, abra a pasta `QualEABoaMobile` (File → Open).
2. Aguarde a sincronização do Gradle (na primeira vez ele baixa as dependências).
3. Escolha um emulador ou celular com Android 7.0 (API 24) ou superior e clique em **Run**.
4. Para rodar os testes: clique com o botão direito em `app/src/test` → *Run Tests*.

Dica para testar fotos no emulador: abra o navegador do emulador, salve uma imagem e depois escolha-a na tela de cadastro.

## O que o aplicativo faz
- Cadastro de usuário e login.
- Lista de eventos com foto, nome, data e local, busca por nome ou local e filtro "Só os que eu tô dentro".
- Criar, editar e apagar eventos com foto da galeria (só quem criou pode editar e apagar).
- "Tô dentro!" / "Não vou mais" e lista de quem vai.
- Na tela do evento: **Ver no mapa** e **Salvar na agenda**.
- Português e inglês, escolhidos pelo idioma do celular.
- Tema claro e escuro, seguindo a configuração do celular.

## Telas e organização do código
| Pacote | Arquivo | Para que serve |
|---|---|---|
| `ui` | `LoginActivity` | Login (primeira tela) |
| `ui` | `CadastroUsuarioActivity` | Criar conta |
| `ui` | `MainActivity` | Lista de eventos, busca e filtro |
| `ui` | `DetalheEventoActivity` | Detalhes, "Tô dentro!", quem vai, mapa e agenda |
| `ui` | `CadastroEventoActivity` | Criar e editar evento |
| `adapter` | `EventoAdapter` | Liga a lista de eventos ao RecyclerView |
| `model` | `Evento`, `Usuario`, `Interesse` | Tabelas do banco (entidades do Room) |
| `data` | `QualEABoaDatabase`, `EventoDao`, `UsuarioDao`, `InteresseDao` | Banco Room e suas operações |
| `util` | `DataUtils`, `SenhaUtils` | Formatar datas e gerar o hash da senha |

## Como cada requisito foi atendido
| Requisito | Onde |
|---|---|
| RF01 – RecyclerView com imagem e nome; Intent explícita para detalhes | `MainActivity.setupRecyclerView()`, `EventoAdapter`, `item_evento.xml` |
| RF02 – Intents implícitas | `DetalheEventoActivity`: `abrirMapa()` (ACTION_VIEW + `geo:`) e `adicionarNaAgenda()` (ACTION_INSERT) |
| RF03 – Cadastro com inserir, editar e excluir | `CadastroEventoActivity` (botão "Criar evento" na tela principal); editar e apagar na tela de detalhes |
| RF04 – Room, imagem salva como URI em String | pacote `data` (padrão da apostila "Persistência de dados": entidade, DAO e Database com Singleton); campo `Evento.foto` |
| RF05 – Inglês e pt-BR automáticos | `values/strings.xml` e `values-pt-rBR/strings.xml` |
| RF06 – Tema claro e escuro | `values/` e `values-night/` (`colors.xml` e `themes.xml`) |
| RNF01 – Kotlin, ViewBinding, RecyclerView, ConstraintLayout | todas as telas |
| RNF02 – Textos, cores e temas em recursos | nenhum texto ou cor fixa nos layouts |
| RNF03 – Pacotes ui, model, data, adapter | ver tabela acima |

## Limitações (projeto acadêmico)
- Os dados ficam só no aparelho (não há servidor). Por isso, quem vai a um evento só pode ser alguém com conta no mesmo celular.
- O banco é recarregado toda vez que a tela principal aparece (`onResume`), como na versão alternativa da apostila.
- A senha não é salva como foi digitada (guardamos um hash SHA-256), mas isso não é um login seguro de verdade. Use apenas dados de teste.
- O banco guarda só o endereço da foto. Se a foto for apagada da galeria, ela some do evento.
