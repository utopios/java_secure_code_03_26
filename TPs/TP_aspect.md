## Exercice : Métriques de performance avec Spring AOP

### Objectif

Créer un aspect qui mesure automatiquement le temps d'exécution de chaque méthode de contrôleur et expose les statistiques via `GET /metrics`.

### Code de départ

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<String> getAll() throws InterruptedException {
        Thread.sleep(new Random().nextInt(200));
        return List.of("Laptop", "Clavier", "Souris");
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(new Random().nextInt(100));
        return "Product " + id;
    }
}

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping
    public List<String> getAll() throws InterruptedException {
        Thread.sleep(new Random().nextInt(300));
        return List.of("Order-001", "Order-002");
    }
}
```

### Consignes

Créez un `@Aspect` qui intercepte toutes les méthodes publiques des `@RestController` (sauf le contrôleur de métriques), mesure le temps d'exécution avec un advice `@Around`, et stocke dans une `ConcurrentHashMap` : le nombre d'appels, le temps total, le temps max. Exposez le tout via `GET /metrics` avec le format :

```json
{
  "ProductController.getAll": {
    "count": 15,
    "avgTimeMs": 98.5,
    "maxTimeMs": 195
  }
}
```

**Contraintes** : thread-safe, enregistrer le temps même en cas d'exception, ne pas mesurer le `MetricsController` lui-même.

**Bonus** : annotation personnalisée `@Tracked` pour cibler des méthodes spécifiques, et `DELETE /metrics` pour remettre à zéro.

