# Wireframes

These are low-fidelity planning wireframes. They describe layout and hierarchy only; they are not implementation code.

## Mobile App Shell

```text
+--------------------------------+
| Header: Screen title     User  |
+--------------------------------+
|                                |
| Screen content                 |
|                                |
|                                |
+--------------------------------+
| Dash | Workouts | Log | Food | AI |
+--------------------------------+
```

The center Log item opens quick actions:

```text
+--------------------------------+
| Quick Log                      |
|                                |
| [ Log Workout ]                |
| [ Log Food    ]                |
| [ Cancel      ]                |
+--------------------------------+
```

## Login

```text
+--------------------------------+
| LiftLog AI                     |
|                                |
| Email                          |
| [________________________]     |
| Password                       |
| [________________________]     |
|                                |
| [ Log in ]                     |
| Create account                 |
+--------------------------------+
```

## Register

```text
+--------------------------------+
| Create account                 |
|                                |
| Display name                   |
| [________________________]     |
| Email                          |
| [________________________]     |
| Password                       |
| [________________________]     |
|                                |
| [ Create account ]             |
| Already have an account?       |
+--------------------------------+
```

## Dashboard

```text
+--------------------------------+
| Dashboard                User  |
+--------------------------------+
| Strength Progression           |
| Bench Press                    |
| Best 100 kg   Recent +2.5 kg   |
| [ simple trend line ]          |
+--------------------------------+
| Protein Today                  |
| 118g / 160g                    |
| [ progress bar ] 42g left      |
+--------------------------------+
| Quick Actions                  |
| [ Log Workout ] [ Log Food ]   |
+--------------------------------+
| Recent Workouts                |
| Upper Body       Jun 16        |
| Lower Body       Jun 14        |
+--------------------------------+
| Active Goals                   |
| Bench 110 kg      74%          |
+--------------------------------+
| AI Coach                       |
| Latest recommendation summary  |
+--------------------------------+
```

## Workout List

```text
+--------------------------------+
| Workouts            [+]        |
+--------------------------------+
| Search exercises or workouts   |
| [________________________]     |
+--------------------------------+
| Upper Body                     |
| Jun 16 | 5 exercises | 8,450kg |
+--------------------------------+
| Lower Body                     |
| Jun 14 | 4 exercises | 10,200kg|
+--------------------------------+
| Push                           |
| Jun 12 | 6 exercises | 7,900kg |
+--------------------------------+
```

## Create Workout

```text
+--------------------------------+
| New Workout              Save  |
+--------------------------------+
| Date                           |
| [ Today                    v ]  |
| Title                          |
| [ Upper Body              ]     |
+--------------------------------+
| Exercises                      |
| [ Add Exercise ]               |
+--------------------------------+
| Bench Press                    |
| Last: 95kg x 8   Best: 100kg   |
| Set | Reps | Weight | Done     |
|  1  | [8 ] | [95  ] | [x]      |
|  2  | [8 ] | [95  ] | [x]      |
| [ Add Set ]                    |
+--------------------------------+
| Notes                          |
| [________________________]     |
+--------------------------------+
```

## Workout Detail

```text
+--------------------------------+
| Upper Body              Edit   |
| Jun 16                         |
+--------------------------------+
| Summary                        |
| 5 exercises | 16 sets | 8,450kg|
+--------------------------------+
| Bench Press        Progress >  |
| 1  8 reps  95kg    done        |
| 2  8 reps  95kg    done        |
+--------------------------------+
| Row                         >  |
| 1  10 reps 70kg    done        |
+--------------------------------+
| Notes                          |
| Felt strong                    |
+--------------------------------+
```

## Exercise Progress

```text
+--------------------------------+
| Bench Press                    |
+--------------------------------+
| Best Weight                    |
| 100 kg                         |
| Recent change +2.5 kg          |
+--------------------------------+
| Strength Trend                 |
| [ line chart ]                 |
+--------------------------------+
| Stats                          |
| Workouts 4                    |
| Completed sets 12              |
| Best reps 10                   |
| Total volume 8,450kg           |
+--------------------------------+
| Recent Sessions                |
| Jun 16  95kg x 8               |
| Jun 09  92.5kg x 8             |
+--------------------------------+
```

## Nutrition Overview

```text
+--------------------------------+
| Nutrition              [+]     |
+--------------------------------+
| Today                          |
| < Jun 16 >                     |
+--------------------------------+
| Protein                        |
| 118g / 160g                    |
| [ progress bar ] 42g left      |
+--------------------------------+
| Calories and Macros            |
| 2,120 kcal                     |
| C 220g   F 65g                 |
+--------------------------------+
| Meals                          |
| Breakfast                      |
| Greek yogurt       30g protein |
| Lunch                          |
| Chicken rice bowl  45g protein |
+--------------------------------+
```

## Nutrition Log

```text
+--------------------------------+
| Log Food                 Save  |
+--------------------------------+
| Date                           |
| [ Today                    v ]  |
| Meal                           |
| [ Lunch                    v ]  |
| Food name                      |
| [ Chicken rice bowl       ]     |
+--------------------------------+
| Protein                        |
| [ 45 ] g                       |
| Calories                       |
| [ 650 ] kcal                   |
| Carbs                          |
| [ 70 ] g                       |
| Fat                            |
| [ 18 ] g                       |
+--------------------------------+
| Notes                          |
| [________________________]     |
+--------------------------------+
```

## Goals

```text
+--------------------------------+
| Goals                  [+]     |
+--------------------------------+
| Strength Goals                 |
| Bench 110 kg                   |
| [ progress bar ] 74%           |
+--------------------------------+
| Nutrition Goals                |
| 160g protein daily             |
| [ progress bar ] 68% today     |
+--------------------------------+
| Other Goals                    |
| Bodyweight check-in            |
| Latest 82kg                    |
+--------------------------------+
```

## AI Coach

```text
+--------------------------------+
| AI Coach                       |
+--------------------------------+
| Ask for feedback               |
| [ Training + nutrition     v ] |
| [ What should I focus on? ]    |
| [ Request Feedback ]           |
+--------------------------------+
| Latest Feedback                |
| Summary text                   |
|                                |
| Recommendations                |
| 1. Next action                 |
| 2. Next action                 |
+--------------------------------+
| History                        |
| Jun 16  Protein consistency    |
| Jun 10  Bench progression      |
+--------------------------------+
```

## Profile

```text
+--------------------------------+
| Profile                        |
+--------------------------------+
| Display Name                   |
| [ Alex                    ]     |
| Email                          |
| user@example.com               |
+--------------------------------+
| Fitness Context                |
| Optional fields when supported |
+--------------------------------+
```

## Settings

```text
+--------------------------------+
| Settings                       |
+--------------------------------+
| Account                        |
| Profile                        |
| Privacy and data               |
+--------------------------------+
| Session                        |
| [ Log out ]                    |
+--------------------------------+
```

## Desktop Adaptation

Desktop should use the same content hierarchy with a left sidebar and wider grids:

```text
+--------------+----------------------------------------------+
| Sidebar      | Header and quick actions                      |
| Dashboard    +----------------------+-----------------------+
| Workouts     | Strength Progression | Protein Today         |
| Nutrition    +----------------------+-----------------------+
| Progress     | Recent Workouts      | Goals / AI Coach      |
| Goals        |                      |                       |
| AI Coach     |                      |                       |
+--------------+----------------------------------------------+
```
