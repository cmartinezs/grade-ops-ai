# Evidence Checklist

Operational checklist for collecting, organizing, and attaching all required evidence for the hackathon submission.

Source of requirements: Devpost rules page and judging criteria. Recheck before final submission at <https://xprize.devpost.com/rules>.

---

## Status Key

| Symbol | Meaning |
|--------|---------|
| `[ ]` | Not collected |
| `[~]` | In progress |
| `[x]` | Collected and ready |

---

## 1. Product Evidence

| Item | How to collect | Status |
|------|---------------|--------|
| Public product URL (deployed, reachable) | Deploy to Cloud Run demo environment | `[ ]` |
| Demo video (under 3 min, English) | Follow [`demo-script.md`](demo-script.md) | `[ ]` |
| GitHub repository link (public, clean README) | Push and verify `grade-ops-ai-docs` and implementation repos | `[ ]` |
| Testing instructions for judges | Create `testing-instructions.md` with login, walkthrough steps | `[ ]` |
| Product screenshots — teacher dashboard | Screenshot during demo environment run | `[ ]` |
| Product screenshots — agent log viewer | Screenshot showing 10+ entries with model/cost/status | `[ ]` |
| Product screenshots — evidence dashboard | Screenshot showing usage and cost summary | `[ ]` |
| Product screenshots — grading queue | Screenshot showing suggestion + approval flow | `[ ]` |

---

## 2. AI / Agent Evidence

| Item | How to collect | Status |
|------|---------------|--------|
| Agent execution log export | Export from product DB or screenshot agent log viewer table | `[ ]` |
| Gemini API usage screenshot | Google Cloud Console → APIs & Services → Credentials / Quota | `[ ]` |
| Vertex AI / Gemini console usage metrics | Cloud Monitoring or Gemini API dashboard screenshot | `[ ]` |
| Model names and versions used | Captured in agent logs; confirm entries show `model_used` field | `[ ]` |
| Estimated cost per assessment run | Visible in evidence dashboard | `[ ]` |

---

## 3. User and Customer Evidence

| Item | How to collect | Status |
|------|---------------|--------|
| User count (educators using the product) | Product DB count + screenshot | `[ ]` |
| User consent documentation | Confirm users agreed to data use for submission purposes | `[ ]` |
| Educator discovery conversations count | `01-business/customer-discovery.md` — count completed interviews | `[ ]` |
| Pilot commitments | Signed or written commitment from at least one pilot teacher | `[ ]` |
| Testimonials (at least 1) | Written quote from a real educator; link to `05-evidence/testimonials.md` | `[ ]` |
| Customer evidence summary | Link to `05-evidence/users.md` | `[ ]` |

---

## 4. Revenue Evidence

The submission must disclose revenue regardless of amount, including zero.

| Item | How to collect | Status |
|------|---------------|--------|
| Total revenue earned | Sum from all sources (Stripe, transfer, invoice, commitment) | `[ ]` |
| Revenue by month | Monthly breakdown | `[ ]` |
| Arms-length revenue flagged | Identify revenue from unrelated parties separately | `[ ]` |
| Related-party revenue flagged | Identify revenue from founders, family, team | `[ ]` |
| Revenue evidence document | Receipt, Stripe export, invoice, bank record, or signed LOI | `[ ]` |
| Pilot Pack sales (if any) | US$99 one-time pilot purchase records | `[ ]` |
| Subscription revenue (if any) | Monthly subscription payment records | `[ ]` |

---

## 5. Cost Evidence

> **GCP Credits:** The hackathon sponsor provides Google Cloud credits. Redeem them before incurring costs — see [`docs/09-developer-guide/00-gcp-project-setup.md`](../09-developer-guide/00-gcp-project-setup.md). The submission must distinguish between cash costs (charged to your payment method) and credit-covered costs. Both must be disclosed. Check the exact redemption process at <https://xprize.devpost.com/rules>.

| Item | How to collect | Status |
|------|---------------|--------|
| GCP credits claimed and amount | Screenshot from Billing → Credits in Google Cloud Console | `[ ]` |
| Total operating costs (runtime) | Billing export: Gemini API + Cloud Run + Cloud SQL + Cloud Storage | `[ ]` |
| Cash costs paid out of pocket | Billing amount charged to payment method (after credits applied) | `[ ]` |
| Credit-covered costs | Billing amount covered by hackathon sponsor credits | `[ ]` |
| Gemini API spend | Google Cloud billing export, filtered by API | `[ ]` |
| Cloud Run spend | Google Cloud billing export | `[ ]` |
| Cloud SQL spend | Google Cloud billing export | `[ ]` |
| Other Google Cloud spend | Google Cloud billing export | `[ ]` |
| Marketing / customer acquisition spend | Amount spent on ads, events, outreach; zero is valid | `[ ]` |
| Cost per graded submission (calculated) | Total runtime cost / total graded submissions | `[ ]` |

---

## 6. Submission Form Fields

Confirm these are completed in Devpost before submitting:

| Field | Content | Status |
|-------|---------|--------|
| Project name | GradeOps AI | `[ ]` |
| Tagline | AI-native assessment operations for programming educators | `[ ]` |
| Demo video URL | YouTube unlisted or Vimeo link | `[ ]` |
| GitHub URL | Public repository | `[ ]` |
| Written description / narrative | See [`submission-narrative.md`](submission-narrative.md) | `[ ]` |
| Revenue disclosure | From revenue evidence above | `[ ]` |
| Cost disclosure | From cost evidence above | `[ ]` |
| User count disclosure | From user evidence above | `[ ]` |
| Agent/API log disclosure | From agent evidence above | `[ ]` |
| Marketing spend disclosure | From cost evidence above | `[ ]` |
| Related-party revenue disclosure | Flagged from revenue evidence | `[ ]` |
| English language confirmed | Narrative and demo in English | `[ ]` |
| Category selected | Education & Human Potential | `[ ]` |
| Google Cloud product confirmed | Cloud Run + Gemini API usage documented | `[ ]` |

---

## 7. Final Pre-Submission Checks

| Check | Status |
|-------|--------|
| Product URL is publicly reachable | `[ ]` |
| Demo video plays correctly at submission URL | `[ ]` |
| GitHub repo is public with readable README | `[ ]` |
| No broken links in submission form | `[ ]` |
| All evidence files are accessible to judges | `[ ]` |
| Submission is confirmed before August 17, 2026 at 1:00 PM PDT | `[ ]` |
| Backup copy of all evidence saved locally | `[ ]` |
