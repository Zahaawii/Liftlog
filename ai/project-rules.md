Architecture
------------
- Follow SOLID principles.
- Follow GRASP principles.
- Use layered architecture.
- No business logic in controllers.
- No direct database access outside repositories.
- Services contain business logic.

Testing
--------
- New business logic requires tests.
- Critical paths require integration tests.
- Bug fixes require regression tests.

Security
---------
- Passwords must be BCrypt hashed.
- Never log credentials.
- Use environment variables for secrets.

Documentation
-------------
- Public APIs must be documented.
- Significant architecture decisions must be recorded.