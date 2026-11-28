<?php

namespace App\Entity;

use App\Entity\User;
use App\Entity\Categorie;
use App\Entity\PhotoAnnonce;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity]
#[ORM\Table(name: 'annonce')]
#[ORM\HasLifecycleCallbacks]
class Annonce
{
    // =========================================
    // ID
    // =========================================

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;


    // =========================================
    // USER RELATIONSHIP
    // =========================================

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(
        name: 'utilisateur_id',
        referencedColumnName: 'id',
        nullable: false
    )]
    private ?User $utilisateur = null;


    // =========================================
    // CATEGORY RELATIONSHIP
    // =========================================

    #[ORM\ManyToOne(
        targetEntity: Categorie::class,
        inversedBy: 'annonces'
    )]
    #[ORM\JoinColumn(
        name: 'categorie_id',
        referencedColumnName: 'id',
        nullable: false
    )]
    private ?Categorie $categorie = null;


    // =========================================
    // ANNOUNCEMENT INFORMATION
    // =========================================

    #[ORM\Column(length: 255)]
    private ?string $titre = null;

    #[ORM\Column(type: Types::TEXT)]
    private ?string $description = null;

    #[ORM\Column(name: 'type_transaction', length: 50)]
    private ?string $typeTransaction = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    private ?string $prix = null;

    #[ORM\Column(name: 'etat_objet', length: 50)]
    private ?string $etatObjet = null;

    #[ORM\Column(length: 50)]
    private ?string $disponibilite = 'DISPONIBLE';


    // =========================================
    // LOCATION INFORMATION
    // =========================================

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $ville = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $quartier = null;

    #[ORM\Column(type: Types::FLOAT, nullable: true)]
    private ?float $latitude = null;

    #[ORM\Column(type: Types::FLOAT, nullable: true)]
    private ?float $longitude = null;


    // =========================================
    // PUBLICATION DATE
    // =========================================

    #[ORM\Column(
        name: 'date_publication',
        type: Types::DATETIME_MUTABLE,
        nullable: true
    )]
    private ?\DateTimeInterface $datePublication = null;


    // =========================================
    // PHOTOS RELATIONSHIP
    // =========================================

    #[ORM\OneToMany(
        mappedBy: 'annonce',
        targetEntity: PhotoAnnonce::class,
        cascade: ['persist'],
        orphanRemoval: true
    )]
    private Collection $photos;


    // =========================================
    // CONSTRUCTOR
    // =========================================

    public function __construct()
    {
        $this->photos = new ArrayCollection();
    }


    // =========================================
    // AUTOMATIC PUBLICATION DATE
    // =========================================

    #[ORM\PrePersist]
    public function setDatePublicationAutomatically(): void
    {
        if ($this->datePublication === null) {
            $this->datePublication = new \DateTime();
        }
    }


    // =========================================
    // ID GETTER
    // =========================================

    public function getId(): ?int
    {
        return $this->id;
    }


    // =========================================
    // USER GETTERS AND SETTERS
    // =========================================

    public function getUtilisateur(): ?User
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(User $utilisateur): static
    {
        $this->utilisateur = $utilisateur;

        return $this;
    }


    // =========================================
    // CATEGORY GETTERS AND SETTERS
    // =========================================

    public function getCategorie(): ?Categorie
    {
        return $this->categorie;
    }

    public function setCategorie(Categorie $categorie): static
    {
        $this->categorie = $categorie;

        return $this;
    }


    // =========================================
    // TITLE
    // =========================================

    public function getTitre(): ?string
    {
        return $this->titre;
    }

    public function setTitre(string $titre): static
    {
        $this->titre = $titre;

        return $this;
    }


    // =========================================
    // DESCRIPTION
    // =========================================

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): static
    {
        $this->description = $description;

        return $this;
    }


    // =========================================
    // TRANSACTION TYPE
    // =========================================

    public function getTypeTransaction(): ?string
    {
        return $this->typeTransaction;
    }

    public function setTypeTransaction(string $typeTransaction): static
    {
        $this->typeTransaction = $typeTransaction;

        return $this;
    }


    // =========================================
    // PRICE
    // =========================================

    public function getPrix(): ?string
    {
        return $this->prix;
    }

    public function setPrix(?string $prix): static
    {
        $this->prix = $prix;

        return $this;
    }


    // =========================================
    // OBJECT CONDITION
    // =========================================

    public function getEtatObjet(): ?string
    {
        return $this->etatObjet;
    }

    public function setEtatObjet(string $etatObjet): static
    {
        $this->etatObjet = $etatObjet;

        return $this;
    }


    // =========================================
    // AVAILABILITY
    // =========================================

    public function getDisponibilite(): ?string
    {
        return $this->disponibilite;
    }

    public function setDisponibilite(string $disponibilite): static
    {
        $this->disponibilite = $disponibilite;

        return $this;
    }


    // =========================================
    // CITY
    // =========================================

    public function getVille(): ?string
    {
        return $this->ville;
    }

    public function setVille(?string $ville): static
    {
        $this->ville = $ville;

        return $this;
    }


    // =========================================
    // NEIGHBORHOOD
    // =========================================

    public function getQuartier(): ?string
    {
        return $this->quartier;
    }

    public function setQuartier(?string $quartier): static
    {
        $this->quartier = $quartier;

        return $this;
    }


    // =========================================
    // LATITUDE
    // =========================================

    public function getLatitude(): ?float
    {
        return $this->latitude;
    }

    public function setLatitude(?float $latitude): static
    {
        $this->latitude = $latitude;

        return $this;
    }


    // =========================================
    // LONGITUDE
    // =========================================

    public function getLongitude(): ?float
    {
        return $this->longitude;
    }

    public function setLongitude(?float $longitude): static
    {
        $this->longitude = $longitude;

        return $this;
    }


    // =========================================
    // PUBLICATION DATE
    // =========================================

    public function getDatePublication(): ?\DateTimeInterface
    {
        return $this->datePublication;
    }

    public function setDatePublication(
        ?\DateTimeInterface $datePublication
    ): static {
        $this->datePublication = $datePublication;

        return $this;
    }


    // =========================================
    // PHOTOS
    // =========================================

    public function getPhotos(): Collection
    {
        return $this->photos;
    }

    public function addPhoto(PhotoAnnonce $photo): static
    {
        if (!$this->photos->contains($photo)) {
            $this->photos->add($photo);
            $photo->setAnnonce($this);
        }

        return $this;
    }

    public function removePhoto(PhotoAnnonce $photo): static
    {
        if ($this->photos->removeElement($photo)) {
            if ($photo->getAnnonce() === $this) {
                // The relationship is removed from this collection.
            }
        }

        return $this;
    }
}
