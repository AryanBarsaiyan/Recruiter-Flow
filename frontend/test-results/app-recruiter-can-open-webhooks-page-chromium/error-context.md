# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e4]:
    - generic [ref=e5]:
      - generic [ref=e6]: Create your company
      - generic [ref=e7]: Sign up as Super Admin to create the first company and start recruiting
    - generic [ref=e8]:
      - generic [ref=e9]:
        - alert [ref=e10]: Something went wrong. Please try again.
        - generic [ref=e11]:
          - generic [ref=e12]: Full name
          - textbox "Full name" [ref=e13]:
            - /placeholder: Jane Doe
            - text: E2E Webhook User
        - generic [ref=e14]:
          - generic [ref=e15]: Company name
          - textbox "Company name" [ref=e16]:
            - /placeholder: Acme Inc
            - text: E2E Web Co mmgtqzab
        - generic [ref=e17]:
          - generic [ref=e18]: Email
          - textbox "Email" [ref=e19]:
            - /placeholder: you@company.com
            - text: e2e-web-mmgtqzab@example.com
        - generic [ref=e20]:
          - generic [ref=e21]: Password
          - textbox "Password" [ref=e22]: password123
      - generic [ref=e23]:
        - button "Create account" [ref=e24]
        - paragraph [ref=e25]:
          - text: Already have an account?
          - link "Sign in" [ref=e26] [cursor=pointer]:
            - /url: /login
  - button "Open Next.js Dev Tools" [ref=e32] [cursor=pointer]:
    - img [ref=e33]
  - alert [ref=e36]
```