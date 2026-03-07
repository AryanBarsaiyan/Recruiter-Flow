# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e4]:
    - generic [ref=e5]:
      - generic [ref=e6]: Sign in
      - generic [ref=e7]: Enter your email and password to access your account
    - generic [ref=e8]:
      - generic [ref=e9]:
        - alert [ref=e10]: Something went wrong. Please try again.
        - generic [ref=e11]:
          - generic [ref=e12]: Email
          - textbox "Email" [ref=e13]:
            - /placeholder: you@company.com
            - text: test-candidate@example.com
        - generic [ref=e14]:
          - generic [ref=e15]: Password
          - textbox "Password" [ref=e16]: password123
      - generic [ref=e17]:
        - button "Sign in" [ref=e18]
        - generic [ref=e19]:
          - link "Forgot password?" [ref=e20] [cursor=pointer]:
            - /url: /request-password-reset
          - generic [ref=e21]:
            - text: Don't have an account?
            - link "Create company (Super Admin)" [ref=e22] [cursor=pointer]:
              - /url: /signup
          - link "Accept invite" [ref=e23] [cursor=pointer]:
            - /url: /accept-invite
          - link "Browse jobs →" [ref=e24] [cursor=pointer]:
            - /url: /jobs
  - button "Open Next.js Dev Tools" [ref=e30] [cursor=pointer]:
    - img [ref=e31]
  - alert [ref=e34]
```