# SIMEPAD

**Système Intelligent de Surveillance Environnementale Maritime – Port Autonome de Dakar**

SIMEPAD est un prototype académique de Master 2 visant à collecter et croiser des données environnementales maritimes, à détecter des anomalies et à fournir aux opérateurs des alertes compréhensibles. Le système est conçu pour le contexte du Port de Dakar. Les résultats du prototype ne constituent pas une certification opérationnelle ou une mesure environnementale officielle.

## État actuel

Le backend est une application Java 21 avec Spring Boot 4.1.1. À ce stade, il permet :

- d’enregistrer des observations manuelles et de les lire depuis PostgreSQL ;
- de collecter les conditions météo courantes via Open-Meteo ;
- de collecter des conditions marines via Open-Meteo Marine ;
- de lancer ces deux collectes manuellement par API ;
- d’activer en option une collecte périodique pour Dakar.

La détection d’anomalies, la gestion de cycle de vie des alertes, l’interface cartographique, l’intégration AIS, les traitements Copernicus et le module cognitif ne sont pas encore implémentés.

## Architecture

Le code suit une architecture hexagonale. Les dépendances vont des adaptateurs vers les ports et les cas d’usage ; le domaine ne dépend pas de Spring, JDBC, PostgreSQL ou d’un fournisseur externe.

```text
com.simepad
├── domain
│   ├── model                 # Observations et instantanés normalisés
│   └── port                  # Interfaces entrantes et sortantes
├── application
│   └── service               # Cas d’usage sans dépendance au framework
├── adapter
│   ├── in/web                # API REST
│   ├── in/scheduling         # Déclenchement périodique optionnel
│   └── out
│       ├── openmeteo         # Adaptateurs HTTP météo et marine
│       └── persistence       # Adaptateur PostgreSQL
└── configuration             # Assemblage des ports et adaptateurs Spring
```

Principes appliqués : dépendance aux abstractions, responsabilités séparées, objets métier validant leurs invariants, contrôleurs limités à l’adaptation HTTP et adaptateurs remplaçables. Les ports de données externes permettent de substituer plus tard un simulateur, Copernicus ou un autre fournisseur sans changer le domaine.

## Technologies

- Java 21, Spring Boot 4.1.1
- Spring MVC / WebClient
- PostgreSQL 16+ avec `JSONB`
- Flyway pour l’évolution du schéma
- Maven Wrapper
- Spring Security présent dans le projet ; l’accès API est actuellement ouvert pour le prototype local

## Prérequis

- JDK 21
- PostgreSQL démarré et base créée
- Accès réseau à Open-Meteo pour les collectes réelles

Créer une base, par exemple :

```sql
CREATE DATABASE simepad;
CREATE USER simepad WITH PASSWORD 'simepad';
GRANT ALL PRIVILEGES ON DATABASE simepad TO simepad;
```

Selon la configuration PostgreSQL, il peut également être nécessaire d’accorder les droits sur le schéma `public` :

```sql
\connect simepad
GRANT ALL ON SCHEMA public TO simepad;
```

Les migrations Flyway sont exécutées au démarrage. La migration initiale crée `environmental_observations` avec un index temporel et un index source/date. Hibernate utilise `ddl-auto=validate` et ne crée pas le schéma.

## Configuration

La configuration par défaut se trouve dans `src/main/resources/application.properties`. Les valeurs de la base peuvent être remplacées par des variables d’environnement :

| Variable | Valeur par défaut | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/simepad` | URL JDBC PostgreSQL |
| `DB_USERNAME` | `simepad` | Utilisateur de la base |
| `DB_PASSWORD` | `simepad` | Mot de passe local de développement ; à remplacer en dehors du poste local |
| `OPEN_METEO_BASE_URL` | `https://api.open-meteo.com/v1/forecast` | Endpoint météo |
| `OPEN_METEO_MARINE_BASE_URL` | `https://marine-api.open-meteo.com/v1/marine` | Endpoint marin |
| `SIMEPAD_COLLECTION_SCHEDULE_ENABLED` | `false` | Active la collecte périodique si `true` |
| `SIMEPAD_COLLECTION_INITIAL_DELAY_MS` | `3600000` | Délai avant la première collecte planifiée |
| `SIMEPAD_COLLECTION_FIXED_DELAY_MS` | `3600000` | Délai entre deux cycles, en millisecondes |

Open-Meteo est utilisé ici pour un prototype non commercial, sans clé. Respecter ses conditions d’utilisation, quotas et obligations d’attribution avant toute diffusion ou usage commercial. Les endpoints météo peuvent changer leurs prévisions ; ils ne représentent pas des capteurs installés dans le port.

Open-Meteo Marine retourne des valeurs issues de modèles. La résolution et la précision côtière sont limitées ; ces valeurs servent au contexte et à l’expérimentation, pas à la navigation ni à une confirmation terrain. Le code enregistre les unités dans le nom des mesures (par exemple `_m`, `_kmh`, `_c`).

L’API peut choisir une cellule marine située à quelques kilomètres des coordonnées demandées ; sa documentation avertit que la précision des courants est limitée près des côtes. Toute interface devra afficher ces valeurs comme des estimations de modèle. Une attribution visible à Open-Meteo et aux fournisseurs des modèles utilisés est requise selon leurs conditions d’utilisation.

## Lancer l’application

Depuis la racine du projet, avec PowerShell :

```powershell
.\mvnw.cmd spring-boot:run
```

Le Wrapper lit les variables exportées dans l’environnement du processus ; Spring Boot ne charge pas automatiquement un fichier `.env`. Pour définir temporairement les paramètres dans PowerShell :

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/simepad'
$env:DB_USERNAME = 'simepad'
$env:DB_PASSWORD = 'simepad'
$env:SIMEPAD_COLLECTION_SCHEDULE_ENABLED = 'false'
.\mvnw.cmd spring-boot:run
```

Le serveur HTTP écoute par défaut sur `http://localhost:8080`. La collecte périodique démarre lorsque la propriété est activée ; la première exécution a lieu après le délai initial configuré. Les collectes manuelles restent disponibles indépendamment du planificateur.

## API REST

Toutes les dates sont des instants ISO-8601 UTC (`2026-10-28T14:00:00Z`). Les mesures reçues manuellement doivent être un objet JSON de nombres finis.

### Créer une observation

```http
POST /api/v1/observations
Content-Type: application/json
```

```json
{
  "source": "manual",
  "location": "Dakar - quai 7",
  "observedAt": "2026-10-28T14:00:00Z",
  "measurements": {
    "water_temperature_c": 26.4,
    "turbidity_ntu": 8.2
  }
}
```

Réponse : `201 Created` avec l’observation et son identifiant UUID.

### Rechercher des observations

```http
GET /api/v1/observations?source=open-meteo&from=2026-10-28T00:00:00Z&to=2026-10-29T00:00:00Z&limit=100
```

Tous les filtres sont optionnels. Le tri se fait par date d’observation décroissante. `limit` vaut 100 par défaut et doit être compris entre 1 et 500.

### Collecter météo et milieu marin à la demande

```http
POST /api/v1/collections/open-meteo
POST /api/v1/collections/open-meteo-marine
```

Coordonnées par défaut : Port de Dakar (`latitude=14.6928`, `longitude=-17.4467`). Les coordonnées peuvent être précisées en query params :

```http
POST /api/v1/collections/open-meteo-marine?latitude=14.6928&longitude=-17.4467
```

Les deux endpoints appellent leur source externe puis enregistrent l’instantané normalisé dans PostgreSQL. Si le fournisseur est indisponible, la requête échoue et l’erreur est journalisée côté serveur.

### Santé

```http
GET /actuator/health
```

## Modèle de données

Chaque ligne de `environmental_observations` contient :

- `id` : UUID généré par l’application ;
- `source` et `location` : provenance et zone décrite ;
- `observed_at` : date fournie par la source ;
- `measurements` : mesures normalisées sous forme d’objet JSONB ;
- `created_at` : date d’insertion en base.

La distinction entre `observed_at` et `created_at` conserve l’écart entre heure de mesure et heure de collecte.

## Tests et vérification

Tests présents dans `src/test/java` : services d’enregistrement/collecte, validation des valeurs et des coordonnées. Les tests des services ne nécessitent pas d’appel réseau réel.

Commandes usuelles :

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Pour vérifier une collecte complète, il faut que PostgreSQL soit disponible, que la migration Flyway passe et que le poste accède au fournisseur Open-Meteo. Les appels réels ne sont pas faits automatiquement par les tests unitaires.

## Limites et prochaines étapes

1. Stabiliser le schéma et la persistance avec des tests d’intégration PostgreSQL.
2. Ajouter des scénarios simulés reproductibles avec vérité terrain.
3. Construire une détection d’anomalies explicable et les entités métier d’alerte.
4. Évaluer précision, rappel, F1, faux positifs et délai de détection.
5. Ajouter l’AIS si une source de données exploitable est disponible ; sinon documenter l’import/simulation.
6. Étudier Copernicus/Sentinel en traitement différé, avec une expérimentation distincte.
7. Ajouter le LLM comme adaptateur optionnel d’explication, jamais comme moteur de décision.
8. Sécuriser les endpoints et remplacer les identifiants locaux avant toute démonstration hors environnement contrôlé.

