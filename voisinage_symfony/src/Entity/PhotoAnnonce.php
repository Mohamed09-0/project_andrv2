<?php

namespace App\Entity;

use App\Repository\PhotoAnnonceRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PhotoAnnonceRepository::class)]
class PhotoAnnonce
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(name: 'url_photo', length: 255, nullable: false)]
    private ?string $url_photo = null;

    #[ORM\ManyToOne(targetEntity: Annonce::class, inversedBy: 'photos')]
    #[ORM\JoinColumn(name: 'annonce_id', referencedColumnName: 'id', nullable: false)]
    private ?Annonce $annonce = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setUrlPhoto(string $url_photo): static
    {
        $this->url_photo = $url_photo;
        return $this;
    }

    public function getUrlPhoto(): ?string
    {
        return $this->url_photo;
    }

    public function setAnnonce(Annonce $annonce): static
    {
        $this->annonce = $annonce;
        return $this;
    }

    public function getAnnonce(): ?Annonce
    {
        return $this->annonce;
    }
}
