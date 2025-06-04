from typing import Tuple
from torch import Tensor, arange, cat, ones_like, outer, polar, view_as_complex, view_as_real



def precompute_freqs_cis(dim: int, end: int, theta: float) -> Tensor:
    freqs = 1.0 / (theta ** (arange(0, dim, 2)[: (dim // 2)].float() / dim))
    t = arange(end, device=freqs.device)
    freqs = outer(t, freqs).float()
    return polar(ones_like(freqs), freqs)  # complex64


def apply_rotary_emb(
    xq: Tensor,
    xk: Tensor,
    freqs_cis: Tensor,
) -> Tuple[Tensor, Tensor]:
    xq_ = view_as_complex(xq.float().reshape(*xq.shape[:-1], -1, 2))
    xk_ = view_as_complex(xk.float().reshape(*xk.shape[:-1], -1, 2))
    freqs_cis = freqs_cis[:, None, :]
    xq_out = view_as_real(xq_ * freqs_cis).flatten(-2)
    xk_out = view_as_real(xk_ * freqs_cis).flatten(-2)
    return xq_out.type_as(xq), xk_out.type_as(xk)


def precompute_freqs_cis_2d(
    dim: int,
    height: int,
    width: int,
    theta: float,
) -> Tensor:
    """
    freqs_cis: 2D complex tensor of shape (height, width, dim // 2) to be indexed by
        (height, width) position tuples
    """
    # (dim / 2) frequency bases
    freqs = 1.0 / (theta ** (arange(0, dim, 2).float() / dim))

    h = arange(height, device=freqs.device)
    w = arange(width, device=freqs.device)

    freqs_h = outer(h, freqs[::2]).float()
    freqs_w = outer(w, freqs[1::2]).float()
    freqs_2d = cat(
        [
            freqs_h[:, None, :].repeat(1, width, 1),
            freqs_w[None, :, :].repeat(height, 1, 1),
        ],
        dim=-1,
    )
    return polar(ones_like(freqs_2d), freqs_2d)
